package com.api.pethotelgo.security

import com.api.pethotelgo.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private const val BEARER_PREFIX = "Bearer "
private const val AUTHORIZATION_HEADER = "Authorization"

/**
 * Populates the SecurityContext from a `Authorization: Bearer <jwt>` header.
 *
 * The token subject is the user id, so the account is re-read on every request: a user
 * that was deactivated or deleted stops being authenticated immediately, without waiting
 * for the access token to expire.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (SecurityContextHolder.getContext().authentication == null) {
            extractBearerToken(request)?.let { authenticate(it, request) }
        }
        filterChain.doFilter(request, response)
    }

    private fun extractBearerToken(request: HttpServletRequest): String? {
        val header = request.getHeader(AUTHORIZATION_HEADER) ?: return null
        return if (header.startsWith(BEARER_PREFIX)) {
            header.substring(BEARER_PREFIX.length).trim().ifBlank { null }
        } else {
            null
        }
    }

    private fun authenticate(token: String, request: HttpServletRequest) {
        val userId = jwtService.parseClaims(token)?.subject ?: return
        val user = userRepository.findById(userId).orElse(null) ?: return

        if (!user.isEnabled) {
            logger.warn("Rejected token for disabled user $userId")
            return
        }

        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, null, user.authorities).apply {
                details = WebAuthenticationDetailsSource().buildDetails(request)
            }
    }
}