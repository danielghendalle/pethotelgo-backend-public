package com.api.pethotelgo.service.impl

import com.api.pethotelgo.exception.PetNotFoundException
import com.api.pethotelgo.exception.StayHistoryNotFoundException
import com.api.pethotelgo.exception.ValidationException
import com.api.pethotelgo.model.entity.StayHistory
import com.api.pethotelgo.repository.StayHistoryRepository
import com.api.pethotelgo.repository.PetRepository
import com.api.pethotelgo.service.StayHistoryService
import org.springframework.stereotype.Service

@Service
class StayHistoryServiceImpl(
    private val stayHistoryRepository: StayHistoryRepository,
    private val petRepository: PetRepository
) : StayHistoryService {

    override fun getAllStayHistories(): List<StayHistory> = stayHistoryRepository.findAll()

    override fun getStayHistoriesByPetId(petId: String): List<StayHistory> {
        petRepository.findById(petId).orElseThrow { PetNotFoundException() }
        return stayHistoryRepository.findByPetId(petId)
    }

    override fun createStayHistory(stayHistory: StayHistory): StayHistory {
        validateStayHistoryData(stayHistory)
        stayHistory.pet?.let { petRef ->
            petRepository.findById(petRef.id).orElseThrow { PetNotFoundException() }
        }
        return stayHistoryRepository.save(stayHistory)
    }

    override fun updateStayHistory(id: String, data: StayHistory): StayHistory {
        val existing = stayHistoryRepository.findById(id)
            .orElseThrow { StayHistoryNotFoundException() }

        validateStayHistoryData(data)

        data.pet?.let { petRef ->
            petRepository.findById(petRef.id).orElseThrow { PetNotFoundException() }
        }

        existing.checkIn = data.checkIn
        existing.checkOut = data.checkOut
        existing.behavior = data.behavior
        existing.notes = data.notes
        data.pet?.let { existing.pet = it }
        data.reservation?.let { existing.reservation = it }

        return stayHistoryRepository.save(existing)
    }

    override fun validateStayHistoryData(stayHistory: StayHistory) {
        if (stayHistory.pet == null || stayHistory.pet!!.id.isBlank()) {
            throw ValidationException("Pet is required for stay history")
        }
        if (stayHistory.checkOut != null && !stayHistory.checkIn.isBefore(stayHistory.checkOut)) {
            throw ValidationException("Check-out must be after check-in")
        }
        if (stayHistory.behavior.isBlank()) {
            throw ValidationException("Behavior description is required")
        }
        if (stayHistory.behavior.length > 500) {
            throw ValidationException("Behavior notes cannot exceed 500 characters")
        }
        if (stayHistory.notes.length > 500) {
            throw ValidationException("Notes cannot exceed 500 characters")
        }
        stayHistory.checkOut?.let { checkOut ->
            val daysDuration = java.time.temporal.ChronoUnit.DAYS.between(stayHistory.checkIn, checkOut)
            if (daysDuration > 30) {
                throw ValidationException("Stay duration cannot exceed 30 days")
            }
        }
    }
}
