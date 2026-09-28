package com.api.pethotelgo.repository

import com.api.pethotelgo.model.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, String> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshToken>
    fun findByUserIdAndRevokedAtIsNull(userId: String): List<RefreshToken>
}
