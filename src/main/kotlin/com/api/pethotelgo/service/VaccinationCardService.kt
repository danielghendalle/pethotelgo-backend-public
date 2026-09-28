package com.api.pethotelgo.service

import com.api.pethotelgo.model.dto.VaccinationCardResponse
import org.springframework.web.multipart.MultipartFile

interface VaccinationCardService {
    fun uploadVaccinationCard(petId: String, file: MultipartFile): VaccinationCardResponse
    fun getVaccinationCard(petId: String): VaccinationCardResponse?
    fun deleteVaccinationCard(petId: String)
}

