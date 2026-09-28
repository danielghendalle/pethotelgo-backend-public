package com.api.pethotelgo.model.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens", indexes = [Index(name = "idx_token_hash", columnList = "token_hash")])
class RefreshToken(
    @Id
    var id: String = UUID.randomUUID().toString(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    /** SHA-256 digest of the opaque token; the token itself is never persisted. */
    @Column(name = "token_hash", unique = true, nullable = false)
    var tokenHash: String = "",

    @Column(nullable = false)
    var expiresAt: Instant = Instant.now(),

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    var revokedAt: Instant? = null
) {
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    fun isRevoked(): Boolean = revokedAt != null

    fun isValid(): Boolean = !isExpired() && !isRevoked()
}
