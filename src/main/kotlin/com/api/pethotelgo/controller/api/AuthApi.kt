package com.api.pethotelgo.controller.api

import com.api.pethotelgo.model.dto.AuthResponse
import com.api.pethotelgo.model.dto.LoginRequest
import com.api.pethotelgo.model.dto.RefreshTokenRequest
import com.api.pethotelgo.model.dto.RegisterRequest
import com.api.pethotelgo.model.dto.UserDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication endpoints (login, register, refresh token)")
interface AuthApi {

    @PostMapping("/login")
    @Operation(
        summary = "Login user",
        description = "Authenticate with email and password, returns a JWT access token and a refresh token",
        responses = [
            ApiResponse(responseCode = "200", description = "Login successful"),
            ApiResponse(responseCode = "401", description = "Invalid email or password")
        ]
    )
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse>

    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Create a new user account and return an authenticated session",
        responses = [
            ApiResponse(responseCode = "201", description = "User registered successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input data"),
            ApiResponse(responseCode = "409", description = "Email already exists")
        ]
    )
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse>

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        summary = "Logout user",
        description = "Revoke all refresh tokens of the authenticated user",
        responses = [
            ApiResponse(responseCode = "204", description = "Logout successful"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun logout(authentication: Authentication): ResponseEntity<Void>

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh token",
        description = "Exchange a valid refresh token for a new access token. The presented refresh token is rotated and invalidated.",
        responses = [
            ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
        ]
    )
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse>

    @GetMapping("/me")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        summary = "Get current user",
        description = "Get the authenticated user information",
        responses = [
            ApiResponse(responseCode = "200", description = "User information retrieved"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserDTO>
}
