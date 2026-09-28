package com.api.pethotelgo.controller

import com.api.pethotelgo.controller.api.VaccinationCardApi
import com.api.pethotelgo.model.dto.VaccinationCardResponse
import org.springframework.http.ResponseEntity
import com.api.pethotelgo.service.VaccinationCardService
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class VaccinationCardController(
    private val vaccinationCardService: VaccinationCardService
) : VaccinationCardApi {

    override fun upload(petId: String, file: MultipartFile): VaccinationCardResponse =
        vaccinationCardService.uploadVaccinationCard(petId, file)

    override fun get(petId: String): ResponseEntity<VaccinationCardResponse> {
        val card = vaccinationCardService.getVaccinationCard(petId)
        return if (card != null) ResponseEntity.ok(card) else ResponseEntity.noContent().build()
    }

    override fun delete(petId: String) =
        vaccinationCardService.deleteVaccinationCard(petId)
}

