package com.api.pethotelgo.service

import com.api.pethotelgo.model.dto.AuthResponse
import com.api.pethotelgo.model.dto.LoginRequest
import com.api.pethotelgo.model.dto.RegisterRequest
import com.api.pethotelgo.model.entity.User

interface AuthService {
    fun login(request: LoginRequest): AuthResponse
    fun register(request: RegisterRequest): AuthResponse
    fun refreshToken(refreshToken: String): AuthResponse
    /** Revokes every active refresh token of the user, so no new access token can be minted. */
    fun logout(userId: String)
    fun getById(userId: String): User
}
