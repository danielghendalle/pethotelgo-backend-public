package com.api.pethotelgo.repository

import com.api.pethotelgo.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}

