package com.api.pethotelgo.repository

import com.api.pethotelgo.model.entity.StayHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StayHistoryRepository : JpaRepository<StayHistory, String> {
    fun findByPetId(petId: String): List<StayHistory>
}

