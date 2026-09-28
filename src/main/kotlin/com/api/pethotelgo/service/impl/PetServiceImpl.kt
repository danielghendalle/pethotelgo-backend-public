package com.api.pethotelgo.service.impl

import com.api.pethotelgo.exception.OwnerNotFoundException
import com.api.pethotelgo.exception.PetNotFoundException
import com.api.pethotelgo.exception.ValidationException
import com.api.pethotelgo.model.dto.CreatePetRequest
import com.api.pethotelgo.model.dto.UpdatePetRequest
import com.api.pethotelgo.model.entity.Pet
import com.api.pethotelgo.repository.PetRepository
import com.api.pethotelgo.repository.OwnerRepository
import com.api.pethotelgo.repository.VaccinationCardRepository
import com.api.pethotelgo.service.PetService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class PetServiceImpl(
    private val petRepository: PetRepository,
    private val ownerRepository: OwnerRepository,
    private val vaccinationCardRepository: VaccinationCardRepository
) : PetService {

    override fun getAllPets(): List<Pet> = petRepository.findAll()

    override fun getPetById(id: String): Pet = petRepository.findById(id)
        .orElseThrow { PetNotFoundException() }

    override fun getPetsByOwnerId(ownerId: String): List<Pet> {
        ownerRepository.findById(ownerId).orElseThrow { OwnerNotFoundException() }
        return petRepository.findByOwnerId(ownerId)
    }

    override fun createPet(request: CreatePetRequest): Pet {
        val owner = ownerRepository.findById(request.ownerId)
            .orElseThrow { OwnerNotFoundException() }

        val pet = Pet(
            id = UUID.randomUUID().toString(),
            owner = owner,
            name = request.name,
            breed = request.breed,
            size = request.size,
            needsSeparateSpace = request.needsSeparateSpace,
            sociability = request.sociability,
            allergies = request.allergies,
            specialCare = request.specialCare,
            feedingSchedule = request.feedingSchedule,
            feedingAmount = request.feedingAmount,
            vaccinationCardUrl = request.vaccinationCardUrl,
            createdAt = LocalDateTime.now()
        )

        validatePetData(pet)
        return petRepository.save(pet)
    }

    override fun updatePet(id: String, data: UpdatePetRequest): Pet {
        val existing = getPetById(id)

        existing.name = data.name
        existing.breed = data.breed
        existing.size = data.size
        existing.needsSeparateSpace = data.needsSeparateSpace
        existing.sociability = data.sociability
        existing.allergies = data.allergies
        existing.specialCare = data.specialCare
        existing.feedingSchedule = data.feedingSchedule
        existing.feedingAmount = data.feedingAmount
        existing.vaccinationCardUrl = vaccinationCardRepository.findByPetId(id)?.fileData ?: data.vaccinationCardUrl

        validatePetData(existing)
        return petRepository.save(existing)
    }

    override fun deletePet(id: String) {
        petRepository.delete(getPetById(id))
    }

    override fun validatePetData(pet: Pet) {
        if (pet.name.isBlank()) throw ValidationException("Pet name is required")
        if (pet.name.length > 50) throw ValidationException("Pet name cannot exceed 50 characters")
        if (pet.breed.isBlank()) throw ValidationException("Pet breed is required")
        if (pet.breed.length > 50) throw ValidationException("Pet breed cannot exceed 50 characters")
        if (pet.feedingSchedule.isBlank()) throw ValidationException("Pet feeding schedule is required")
        if (pet.feedingAmount.isBlank()) throw ValidationException("Pet feeding amount is required")

        pet.vaccinationCardUrl?.takeIf { it.isNotBlank() }?.let {
            if (!isValidVaccinationCardUrl(it)) {
                throw ValidationException("Invalid vaccination card URL format. Must be a valid URL or base64 data URI")
            }
        }

        if (pet.owner == null || pet.owner!!.id.isBlank()) {
            throw ValidationException("Pet must be associated with an owner")
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            java.net.URI(url).toURL()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun isValidVaccinationCardUrl(url: String): Boolean {
        return isValidUrl(url) || (url.startsWith("data:") && url.contains(";base64,"))
    }
}
