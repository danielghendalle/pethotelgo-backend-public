package com.api.pethotelgo.model.dto

data class CreateOwnerRequest(
    val name: String,
    val phone: String,
    val email: String
)
