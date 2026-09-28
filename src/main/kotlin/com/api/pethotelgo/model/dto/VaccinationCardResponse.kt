package com.api.pethotelgo.model.dto

import java.time.LocalDateTime

data class VaccinationCardResponse(
    val id: String,
    val petId: String,
    val url: String,
    val fileId: String,
    val fileName: String,
    val fileSize: Long?,
    val fileType: String?,
    val uploadedAt: LocalDateTime
)

