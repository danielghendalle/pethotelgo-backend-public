package com.api.pethotelgo.service

import com.api.pethotelgo.model.dto.CreatePetRequest
import com.api.pethotelgo.model.dto.UpdatePetRequest
import com.api.pethotelgo.model.entity.Pet

interface PetService {
    fun getAllPets(): List<Pet>
    fun getPetById(id: String): Pet
    fun getPetsByOwnerId(ownerId: String): List<Pet>
    fun createPet(request: CreatePetRequest): Pet
    fun updatePet(id: String, data: UpdatePetRequest): Pet
    fun deletePet(id: String)
    fun validatePetData(pet: Pet)
}

