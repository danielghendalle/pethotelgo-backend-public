package com.api.pethotelgo.model.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "owners")
class Owner(
    @Id
    var id: String = UUID.randomUUID().toString(),

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var phone: String = "",

    @Column(nullable = false)
    var email: String = "",

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    var pets: MutableList<Pet> = mutableListOf(),

    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    var reservations: MutableList<Reservation> = mutableListOf()
)