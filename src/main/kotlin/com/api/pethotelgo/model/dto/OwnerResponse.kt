package com.api.pethotelgo.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class OwnerResponse(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    val createdAt: LocalDateTime
)
