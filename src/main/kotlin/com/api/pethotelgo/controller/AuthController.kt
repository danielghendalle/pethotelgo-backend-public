package com.api.pethotelgo.controller

import com.api.pethotelgo.controller.api.AuthApi
import com.api.pethotelgo.model.dto.AuthResponse
import com.api.pethotelgo.model.dto.LoginRequest
import com.api.pethotelgo.model.dto.RefreshTokenRequest
import com.api.pethotelgo.model.dto.RegisterRequest
import com.api.pethotelgo.model.dto.UserDTO
import com.api.pethotelgo.model.entity.User
import com.api.pethotelgo.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(private val authService: AuthService) : AuthApi {

    override fun login(request: LoginRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(request))

    override fun register(request: RegisterRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request))

    override fun logout(authentication: Authentication): ResponseEntity<Void> {
        authService.logout(currentUser(authentication).id)
        return ResponseEntity.noContent().build()
    }

    override fun refreshToken(request: RefreshTokenRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.refreshToken(request.refreshToken))

    override fun getCurrentUser(authentication: Authentication): ResponseEntity<UserDTO> =
        ResponseEntity.ok(currentUser(authentication).toDTO())

    private fun currentUser(authentication: Authentication): User =
        authentication.principal as User
}
