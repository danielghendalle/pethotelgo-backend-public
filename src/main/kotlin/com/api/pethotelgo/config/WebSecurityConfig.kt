package com.api.pethotelgo.config

import com.api.pethotelgo.security.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.http.HttpStatus
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

private const val CORS_ALLOW_ALL = "/**"
private const val CONTENT_TYPE = "Content-Type"
private const val AUTHORIZATION = "Authorization"
private const val CACHE_CONTROL = "Cache-Control"

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    @Value("\${app.cors.allowed-origins:}")
    private val allowedOrigins: String,
    @Value("\${app.swagger.enabled:false}")
    private val swaggerEnabled: Boolean
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { authorizer ->
                authorizer
                    .requestMatchers("/auth/register", "/auth/login", "/auth/refresh").permitAll()

                if (swaggerEnabled) {
                    authorizer.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                }

                authorizer
                    .requestMatchers("/actuator/health").permitAll()
                    .anyRequest().authenticated()
            }
            // Missing/invalid credentials must answer 401, not a redirect to a login form.
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder
    ): AuthenticationManager {
        val provider = DaoAuthenticationProvider(userDetailsService)
        provider.setPasswordEncoder(passwordEncoder)
        return ProviderManager(provider)
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration().apply {
            val origins = parseCorsOrigins()
            // Spring rejects allowedOrigins = ["*"] together with allowCredentials = true at
            // request time (500 on every POST/OPTIONS). When no explicit origin list is
            // configured, fall back to allowedOriginPatterns, which is the credential-safe form.
            if (origins == listOf("*")) {
                allowedOriginPatterns = listOf("*")
            } else {
                allowedOrigins = origins
            }
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf(AUTHORIZATION, CACHE_CONTROL, CONTENT_TYPE)
            allowCredentials = true
        }
        source.registerCorsConfiguration(CORS_ALLOW_ALL, config)
        return source
    }

    private fun parseCorsOrigins(): List<String> =
        if (allowedOrigins.isNotBlank()) {
            allowedOrigins.split(',').map { it.trim() }
        } else {
            listOf("*")
        }
}
