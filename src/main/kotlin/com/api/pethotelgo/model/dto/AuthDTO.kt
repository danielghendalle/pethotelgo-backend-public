package com.api.pethotelgo.model.dto

data class UserDTO(
    val id: String,
    val email: String,
    val name: String,
    val role: String
)

data class AuthResponse(
    val user: UserDTO,
    val token: String,
    val refreshToken: String,
    /** Access token lifetime in seconds. */
    val expiresIn: Long,
    val tokenType: String = "Bearer"
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class TokenResponse(
    val token: String,
    val refreshToken: String
)

