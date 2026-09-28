package com.api.pethotelgo.service.impl

import com.api.pethotelgo.exception.PetNotFoundException
import com.api.pethotelgo.exception.ValidationException
import com.api.pethotelgo.model.dto.VaccinationCardResponse
import com.api.pethotelgo.model.entity.VaccinationCard
import com.api.pethotelgo.repository.PetRepository
import com.api.pethotelgo.repository.VaccinationCardRepository
import com.api.pethotelgo.service.VaccinationCardService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.Base64

@Service
@Transactional
class VaccinationCardServiceImpl(
    private val repository: VaccinationCardRepository,
    private val petRepository: PetRepository
) : VaccinationCardService {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val validFileTypes = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
        "application/pdf"
    )

    override fun uploadVaccinationCard(petId: String, file: MultipartFile): VaccinationCardResponse {
        val pet = petRepository.findById(petId).orElseThrow { PetNotFoundException() }

        if (file.size > 10 * 1024 * 1024) {
            throw ValidationException("File too large. Maximum allowed size is 10 MB")
        }

        if (file.contentType !in validFileTypes) {
            throw ValidationException("Invalid file type. Allowed types: JPEG, PNG, WEBP, PDF")
        }

        val dataUri = "data:${file.contentType ?: "application/octet-stream"};base64,${
            Base64.getEncoder().encodeToString(file.bytes)
        }"

        val existingCard = repository.findByPetId(petId)

        val card = if (existingCard != null) {
            existingCard.fileName = file.originalFilename ?: "vaccination-card"
            existingCard.fileSize = file.size
            existingCard.fileType = file.contentType
            existingCard.fileData = dataUri
            existingCard.updatedAt = LocalDateTime.now()
            existingCard
        } else {
            VaccinationCard(
                petId = petId,
                fileName = file.originalFilename ?: "vaccination-card",
                fileSize = file.size,
                fileType = file.contentType,
                fileData = dataUri
            )
        }

        val saved = repository.save(card)

        pet.vaccinationCardUrl = dataUri
        petRepository.save(pet)

        return saved.toResponse()
    }

    @Transactional(readOnly = true)
    override fun getVaccinationCard(petId: String): VaccinationCardResponse? {
        val card = repository.findByPetId(petId)
        if (card != null) {
            return card.toResponse()
        }

        val pet = petRepository.findById(petId).orElse(null)
        val petBase64 = pet?.vaccinationCardUrl?.takeIf { it.isNotBlank() }

        if (petBase64 != null) {
            return VaccinationCardResponse(
                id = "pet-$petId",
                petId = petId,
                url = petBase64,
                fileId = "pet-$petId",
                fileName = "vaccination-card",
                fileSize = null,
                fileType = null,
                uploadedAt = pet.createdAt
            )
        }

        return null
    }

    override fun deleteVaccinationCard(petId: String) {
        repository.findByPetId(petId)?.let { card ->
            repository.delete(card)

            val pet = petRepository.findById(petId).orElse(null)
            if (pet != null) {
                pet.vaccinationCardUrl = null
                petRepository.save(pet)
                logger.info("Cleared vaccinationCardUrl for pet: $petId")
            }
        }
    }

    private fun VaccinationCard.toResponse() = VaccinationCardResponse(
        id = id,
        petId = petId,
        url = fileData,
        fileId = id,
        fileName = fileName,
        fileSize = fileSize,
        fileType = fileType,
        uploadedAt = uploadedAt
    )
}
