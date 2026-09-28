package com.api.pethotelgo.repository

import com.api.pethotelgo.model.entity.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReservationRepository : JpaRepository<Reservation, String> {
    fun findByPetId(petId: String): List<Reservation>
    // additional helper to find reservations intersecting a day could be implemented if needed
}

