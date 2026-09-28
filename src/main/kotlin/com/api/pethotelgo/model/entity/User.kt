package com.api.pethotelgo.model.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import com.api.pethotelgo.model.enums.UserRole
import com.api.pethotelgo.model.dto.UserDTO

@Entity
@Table(name = "users")
class User(
    @Id
    var id: String = UUID.randomUUID().toString(),

    @Column(unique = true, nullable = false)
    var email: String = "",

    @Column(nullable = false)
    var name: String = "",

    /** BCrypt digest of the user's password. */
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.USER,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var refreshTokens: MutableList<RefreshToken> = mutableListOf()
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
    }

    override fun getPassword(): String = passwordHash

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = isActive

    override fun isAccountNonLocked(): Boolean = isActive

    override fun isCredentialsNonExpired(): Boolean = isActive

    override fun isEnabled(): Boolean = isActive

    fun toDTO(): UserDTO = UserDTO(
        id = this.id,
        email = this.email,
        name = this.name,
        role = this.role.name
    )
}

