package com.api.pethotelgo.security

import com.api.pethotelgo.model.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.Date
import javax.crypto.SecretKey

private const val CLAIM_ROLE = "role"
private const val CLAIM_NAME = "name"
private const val MIN_SECRET_BYTES = 32

/**
 * Issues and validates the stateless access tokens (HS256) and generates the opaque
 * refresh tokens. Refresh tokens are never JWTs: they are random 256-bit values whose
 * SHA-256 digest is what gets persisted, so a database leak cannot be replayed.
 */
@Component
class JwtService(
    @Value("\${app.jwt.secret}")
    private val secret: String,
    @Value("\${app.jwt.expiration-ms:3600000}")
    private val accessTokenExpirationMs: Long,
    @Value("\${app.jwt.refresh-expiration-ms:604800000}")
    val refreshTokenExpirationMs: Long
) {

    // Built eagerly so a weak or missing secret aborts startup instead of surfacing
    // as a 500 on the first login.
    private val signingKey: SecretKey = run {
        val keyBytes = decodeSecret(secret)
        require(keyBytes.size >= MIN_SECRET_BYTES) {
            "app.jwt.secret must decode to at least $MIN_SECRET_BYTES bytes (256 bits). " +
                "Generate one with: openssl rand -base64 64"
        }
        Keys.hmacShaKeyFor(keyBytes)
    }

    private val secureRandom = SecureRandom()

    fun generateAccessToken(user: User): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(user.id)
            .claim(CLAIM_ROLE, user.role.name)
            .claim(CLAIM_NAME, user.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
            .signWith(signingKey)
            .compact()
    }

    /** Returns the token claims, or null when the token is malformed, tampered with or expired. */
    fun parseClaims(token: String): Claims? = try {
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    } catch (_: JwtException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    fun accessTokenExpiresInSeconds(): Long = accessTokenExpirationMs / 1000

    /** Generates a fresh opaque refresh token. Only its [hashRefreshToken] digest is stored. */
    fun generateRefreshToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun hashRefreshToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    /** Accepts either a base64-encoded secret or a raw passphrase. */
    private fun decodeSecret(raw: String): ByteArray = try {
        Base64.getDecoder().decode(raw.trim())
    } catch (_: IllegalArgumentException) {
        raw.toByteArray()
    }
}
