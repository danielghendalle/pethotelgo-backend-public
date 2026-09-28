package com.api.pethotelgo.model.dto

data class UpdateOwnerRequest(
    val name: String,
    val email: String,
    val phone: String
)
