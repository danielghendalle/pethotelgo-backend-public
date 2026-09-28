package com.api.pethotelgo.controller

import com.api.pethotelgo.controller.api.ReservationApi
import com.api.pethotelgo.exception.ValidationException
import com.api.pethotelgo.model.dto.CreateReservationRequest
import com.api.pethotelgo.model.entity.Reservation
import com.api.pethotelgo.service.ReservationService
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class ReservationController(private val reservationService: ReservationService) : ReservationApi {

    override fun getAll(date: LocalDate?): List<Reservation> {
        return if (date != null) {
            reservationService.getReservationsByDate(date)
        } else {
            reservationService.getAllReservations()
        }
    }

    override fun getById(id: String): Reservation = reservationService.getReservationById(id)

    override fun getByPetPath(petId: String): List<Reservation> = reservationService.getReservationsByPetId(petId)

    override fun create(request: CreateReservationRequest): Reservation = reservationService.createReservation(request)

    override fun update(id: String, data: Reservation): Reservation =
        reservationService.updateReservation(id, data)

    override fun delete(id: String) = reservationService.deleteReservation(id)

    override fun updateStatus(id: String, body: Map<String, String>): Reservation {
        val status = body["status"] ?: throw ValidationException("Status is required")
        return reservationService.updateReservationStatus(id, status)
    }
}
