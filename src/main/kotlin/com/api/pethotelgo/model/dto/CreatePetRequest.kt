package com.api.pethotelgo.model.dto

import com.api.pethotelgo.model.enums.PetSize
import com.api.pethotelgo.model.enums.SociabilityLevel

data class CreatePetRequest(
    val ownerId: String,
    val name: String,
    val breed: String,
    val size: PetSize,
    val needsSeparateSpace: Boolean,
    val sociability: SociabilityLevel,
    val allergies: String,
    val specialCare: String,
    val feedingSchedule: String,
    val feedingAmount: String,
    val vaccinationCardUrl: String? = null
)
