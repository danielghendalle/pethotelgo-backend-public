package com.api.pethotelgo.repository

import com.api.pethotelgo.model.entity.Pet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PetRepository : JpaRepository<Pet, String> {
    fun findByOwnerId(ownerId: String): List<Pet>
}

