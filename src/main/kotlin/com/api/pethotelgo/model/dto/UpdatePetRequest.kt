package com.api.pethotelgo.model.dto

import com.api.pethotelgo.model.enums.PetSize
import com.api.pethotelgo.model.enums.SociabilityLevel

data class UpdatePetRequest(
    val name: String,
    val breed: String,
    val size: PetSize,
    val sociability: SociabilityLevel,
    val needsSeparateSpace: Boolean,
    val allergies: String,
    val specialCare: String,
    val feedingSchedule: String,
    val feedingAmount: String,
    val vaccinationCardUrl: String?
)
