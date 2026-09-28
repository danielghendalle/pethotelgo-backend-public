package com.api.pethotelgo.repository

import com.api.pethotelgo.model.entity.Owner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OwnerRepository : JpaRepository<Owner, String>

