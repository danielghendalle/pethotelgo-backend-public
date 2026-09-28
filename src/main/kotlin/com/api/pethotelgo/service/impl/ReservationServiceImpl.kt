package com.api.pethotelgo.service.impl

import com.api.pethotelgo.exception.*
import com.api.pethotelgo.model.entity.Reservation
import com.api.pethotelgo.model.dto.CreateReservationRequest
import com.api.pethotelgo.model.dto.DayCapacity
import com.api.pethotelgo.model.enums.ReservationStatus
import com.api.pethotelgo.repository.ReservationRepository
import com.api.pethotelgo.repository.PetRepository
import com.api.pethotelgo.repository.OwnerRepository
import com.api.pethotelgo.service.AppSettingsService
import com.api.pethotelgo.service.ReservationService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.ZoneId
import java.math.BigDecimal

@Service
class ReservationServiceImpl(
    private val reservationRepository: ReservationRepository,
    private val petRepository: PetRepository,
    private val ownerRepository: OwnerRepository,
    private val appSettingsService: AppSettingsService
) : ReservationService {

    companion object {
        private const val MAX_PETS_PER_DAY = 10
        private const val MIN_RESERVATION_DAYS = 1
    }

    override fun getAllReservations(): List<Reservation> = reservationRepository.findAll()

    override fun getReservationById(id: String): Reservation = reservationRepository.findById(id)
        .orElseThrow { ReservationNotFoundException() }

    override fun getReservationsByDate(date: LocalDate): List<Reservation> {
        val zone = ZoneId.systemDefault()
        return reservationRepository.findAll().filter { r ->
            val start = r.checkIn.atZone(zone).toLocalDate()
            val end = r.checkOut.atZone(zone).toLocalDate()
            !date.isBefore(start) && !date.isAfter(end)
        }
    }

    override fun getReservationsByPetId(petId: String): List<Reservation> {
        petRepository.findById(petId).orElseThrow { PetNotFoundException() }
        return reservationRepository.findByPetId(petId)
    }

    override fun createReservation(request: CreateReservationRequest): Reservation {
        val pet = petRepository.findById(request.petId).orElseThrow { PetNotFoundException() }
        val owner = ownerRepository.findById(request.ownerId).orElseThrow { OwnerNotFoundException() }

        // Use the rate the operator typed in, if any; otherwise the configurable
        // default for the pet's size (Configurações -> valores da hospedagem).
        val dailyRate = request.dailyRate?.takeIf { it > BigDecimal.ZERO }
            ?: appSettingsService.dailyRateFor(pet.size)

        val nights = java.time.temporal.ChronoUnit.DAYS.between(
            request.checkIn.toLocalDate(),
            request.checkOut.toLocalDate()
        )
        val subtotal = dailyRate.multiply(BigDecimal(nights))

        val totalAmount = if (request.discountPercentage != null && request.discountPercentage > BigDecimal.ZERO) {
            val discountAmount = subtotal.multiply(request.discountPercentage.divide(BigDecimal("100")))
            subtotal.subtract(discountAmount)
        } else {
            subtotal
        }

        val reservation = Reservation(
            id = java.util.UUID.randomUUID().toString(),
            pet = pet,
            owner = owner,
            checkIn = request.checkIn,
            checkOut = request.checkOut,
            status = request.status,
            notes = request.notes,
            dailyRate = dailyRate,
            discountPercentage = request.discountPercentage,
            createdAt = LocalDateTime.now()
        )

        return saveReservation(reservation)
    }

    private fun saveReservation(reservation: Reservation): Reservation {
        validateReservationData(reservation)

        reservation.pet?.let { petRef ->
            petRepository.findById(petRef.id).orElseThrow { PetNotFoundException() }
        }

        reservation.owner?.let { ownerRef ->
            ownerRepository.findById(ownerRef.id).orElseThrow { OwnerNotFoundException() }
        }

        val petId = reservation.pet?.id ?: throw ValidationException("Pet is required")
        val existingReservations = reservationRepository.findByPetId(petId)
            .filter { it.status in listOf(ReservationStatus.confirmed, ReservationStatus.pending) }

        if (hasConflict(reservation, existingReservations)) {
            throw ConflictException(
                message = "Pet already has a reservation during this period",
                code = ErrorCode.RESERVATION_CONFLICT
            )
        }

        val zone = ZoneId.systemDefault()
        var currentDate = reservation.checkIn.atZone(zone).toLocalDate()
        val endDate = reservation.checkOut.atZone(zone).toLocalDate()

        while (!currentDate.isAfter(endDate)) {
            val dayCapacity = checkDayCapacity(currentDate)
            if (dayCapacity.isFull) {
                throw ApiException(ErrorCode.CAPACITY_FULL, "Hotel is at full capacity on $currentDate")
            }
            currentDate = currentDate.plusDays(1)
        }

        return reservationRepository.save(reservation)
    }

    override fun updateReservation(id: String, data: Reservation): Reservation {
        val existing = getReservationById(id)
        validateReservationData(data, isUpdate = true)

        if (existing.status in listOf(ReservationStatus.completed, ReservationStatus.cancelled)) {
            throw BusinessRuleException("Cannot update a ${existing.status} reservation")
        }

        data.pet?.let { petRef ->
            petRepository.findById(petRef.id).orElseThrow { PetNotFoundException() }
        }

        data.owner?.let { ownerRef ->
            ownerRepository.findById(ownerRef.id).orElseThrow { OwnerNotFoundException() }
        }

        existing.checkIn = data.checkIn
        existing.checkOut = data.checkOut
        existing.notes = data.notes
        existing.dailyRate = data.dailyRate
        existing.discountPercentage = data.discountPercentage
        data.pet?.let { existing.pet = it }
        data.owner?.let { existing.owner = it }

        return reservationRepository.save(existing)
    }

    override fun deleteReservation(id: String) {
        val reservation = getReservationById(id)
        if (reservation.status != ReservationStatus.pending) {
            throw BusinessRuleException("Can only delete pending reservations")
        }
        reservationRepository.delete(reservation)
    }

    override fun updateReservationStatus(id: String, newStatus: String): Reservation {
        val reservation = getReservationById(id)

        val status = try {
            ReservationStatus.valueOf(newStatus)
        } catch (_: Exception) {
            throw ValidationException("Invalid reservation status")
        }

        if (!isValidStatusTransition(reservation.status, status)) {
            throw BusinessRuleException("Cannot transition from ${reservation.status} to $status")
        }

        reservation.status = status
        return reservationRepository.save(reservation)
    }

    override fun checkDayCapacity(date: LocalDate): DayCapacity {
        val zone = ZoneId.systemDefault()
        val reservations = reservationRepository.findAll().filter { r ->
            if (r.status !in listOf(ReservationStatus.confirmed, ReservationStatus.pending)) return@filter false
            val start = r.checkIn.atZone(zone).toLocalDate()
            val end = r.checkOut.atZone(zone).toLocalDate()
            !date.isBefore(start) && !date.isAfter(end)
        }

        return DayCapacity(
            date = date,
            totalPets = reservations.size,
            isFull = reservations.size >= MAX_PETS_PER_DAY,
            reservations = reservations
        )
    }

    override fun validateReservationData(reservation: Reservation, isUpdate: Boolean) {
        if (reservation.pet == null || reservation.pet!!.id.isBlank()) {
            throw ValidationException("Pet is required")
        }
        if (reservation.owner == null || reservation.owner!!.id.isBlank()) {
            throw ValidationException("Owner is required")
        }
        if (!reservation.checkIn.isBefore(reservation.checkOut)) {
            throw ValidationException("Check-in must be before check-out")
        }

        val daysDifference = java.time.temporal.ChronoUnit.DAYS.between(
            reservation.checkIn.atZone(ZoneId.systemDefault()).toLocalDate(),
            reservation.checkOut.atZone(ZoneId.systemDefault()).toLocalDate()
        )
        if (daysDifference < MIN_RESERVATION_DAYS) {
            throw ValidationException("Reservation must be at least $MIN_RESERVATION_DAYS day(s)")
        }
        // Reservas já existentes podem ter check-in no passado (estadia em andamento);
        // só bloqueia data passada ao criar uma reserva nova. Compara só a data
        // (não o horário exato) para permitir reservar no dia atual.
        if (!isUpdate && reservation.checkIn.toLocalDate().isBefore(LocalDate.now())) {
            throw ValidationException("Cannot create reservation in the past")
        }
        if (daysDifference > 30) {
            throw ValidationException("Reservation cannot exceed 30 days")
        }
    }

    private fun hasConflict(newReservation: Reservation, existingReservations: List<Reservation>): Boolean {
        return existingReservations.any { existing ->
            newReservation.checkIn.isBefore(existing.checkOut) && newReservation.checkOut.isAfter(existing.checkIn)
        }
    }

    private fun isValidStatusTransition(from: ReservationStatus, to: ReservationStatus): Boolean {
        return when {
            from == to -> true
            from == ReservationStatus.pending && to in listOf(ReservationStatus.confirmed, ReservationStatus.cancelled) -> true
            from == ReservationStatus.confirmed && to in listOf(ReservationStatus.completed, ReservationStatus.cancelled) -> true
            else -> false
        }
    }
}
