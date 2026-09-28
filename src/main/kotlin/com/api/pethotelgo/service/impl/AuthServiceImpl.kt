package com.api.pethotelgo.service.impl

import com.api.pethotelgo.exception.ConflictException
import com.api.pethotelgo.exception.ValidationException
import com.api.pethotelgo.model.dto.AuthResponse
import com.api.pethotelgo.model.dto.LoginRequest
import com.api.pethotelgo.model.dto.RegisterRequest
import com.api.pethotelgo.model.entity.RefreshToken
import com.api.pethotelgo.model.entity.User
import com.api.pethotelgo.model.enums.UserRole
import com.api.pethotelgo.repository.RefreshTokenRepository
import com.api.pethotelgo.repository.UserRepository
import com.api.pethotelgo.security.JwtService
import com.api.pethotelgo.service.AuthService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

private const val MIN_PASSWORD_LENGTH = 8
private const val MAX_PASSWORD_LENGTH = 72 // BCrypt truncates beyond 72 bytes
private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) : AuthService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun login(request: LoginRequest): AuthResponse {
        val email = request.email.trim().lowercase()

        val user = try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(email, request.password)
            ).principal as User
        } catch (e: AuthenticationException) {
            // Deliberately generic: never reveal whether the email exists.
            logger.info("Failed login attempt for {}: {}", email, e.javaClass.simpleName)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        }

        return issueTokens(user)
    }

    @Transactional
    override fun register(request: RegisterRequest): AuthResponse {
        validateRegisterRequest(request)
        val email = request.email.trim().lowercase()

        if (userRepository.existsByEmail(email)) {
            throw ConflictException("Email already registered")
        }

        val user = userRepository.save(
            User(
                email = email,
                name = request.name.trim(),
                passwordHash = requireNotNull(passwordEncoder.encode(request.password)) {
                    "Password encoder returned no hash"
                },
                role = UserRole.USER
            )
        )

        return issueTokens(user)
    }

    // noRollbackFor: rejecting a token is signalled by throwing, but the revocations decided
    // along the way (token rotation, and dropping the family on a replay) must still commit.
    @Transactional(noRollbackFor = [ResponseStatusException::class])
    override fun refreshToken(refreshToken: String): AuthResponse {
        if (refreshToken.isBlank()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is required")
        }

        val stored = refreshTokenRepository.findByTokenHash(jwtService.hashRefreshToken(refreshToken))
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token") }

        val user = stored.user
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")

        if (!stored.isValid()) {
            // A revoked token being replayed means it likely leaked: drop the whole family.
            if (stored.isRevoked()) {
                logger.warn("Replay of revoked refresh token for user {} - revoking all sessions", user.id)
                revokeAllTokens(user.id)
            }
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked")
        }

        if (!user.isEnabled) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User account is disabled")
        }

        // Rotation: the presented token is burned before a new one is handed out.
        stored.revokedAt = Instant.now()
        refreshTokenRepository.save(stored)

        return issueTokens(user)
    }

    @Transactional
    override fun logout(userId: String) {
        revokeAllTokens(userId)
    }

    override fun getById(userId: String): User =
        userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

    private fun issueTokens(user: User): AuthResponse {
        val refreshToken = jwtService.generateRefreshToken()

        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                tokenHash = jwtService.hashRefreshToken(refreshToken),
                expiresAt = Instant.now().plusMillis(jwtService.refreshTokenExpirationMs)
            )
        )

        return AuthResponse(
            user = user.toDTO(),
            token = jwtService.generateAccessToken(user),
            refreshToken = refreshToken,
            expiresIn = jwtService.accessTokenExpiresInSeconds()
        )
    }

    private fun revokeAllTokens(userId: String) {
        val active = refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId)
        val now = Instant.now()
        active.forEach { it.revokedAt = now }
        refreshTokenRepository.saveAll(active)
    }

    private fun validateRegisterRequest(request: RegisterRequest) {
        if (request.name.isBlank()) throw ValidationException("Name is required")
        if (request.name.length > 100) throw ValidationException("Name cannot exceed 100 characters")
        if (request.email.isBlank()) throw ValidationException("Email is required")
        if (!EMAIL_REGEX.matches(request.email.trim())) throw ValidationException("Invalid email format")
        if (request.password.isBlank()) throw ValidationException("Password is required")
        if (request.password.length < MIN_PASSWORD_LENGTH) {
            throw ValidationException("Password must be at least $MIN_PASSWORD_LENGTH characters")
        }
        if (request.password.toByteArray().size > MAX_PASSWORD_LENGTH) {
            throw ValidationException("Password cannot exceed $MAX_PASSWORD_LENGTH bytes")
        }
    }
}
