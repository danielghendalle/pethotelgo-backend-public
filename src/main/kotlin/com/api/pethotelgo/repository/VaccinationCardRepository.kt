package com.api.pethotelgo.repository

import com.api.pethotelgo.model.entity.VaccinationCard
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VaccinationCardRepository : JpaRepository<VaccinationCard, String> {
    fun findByPetId(petId: String): VaccinationCard?
}

