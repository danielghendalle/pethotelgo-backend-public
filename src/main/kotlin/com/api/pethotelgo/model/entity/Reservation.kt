package com.api.pethotelgo.model.entity

import com.api.pethotelgo.model.entity.Owner
import com.api.pethotelgo.model.enums.ReservationStatus
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "reservations")
class Reservation(
    @Id
    var id: String = UUID.randomUUID().toString(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    var pet: Pet? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    var owner: Owner? = null,

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    var checkIn: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    var checkOut: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReservationStatus = ReservationStatus.pending,

    @Column(columnDefinition = "text", nullable = false)
    var notes: String = "",

    @Column(precision = 10, scale = 2)
    var dailyRate: BigDecimal? = null,

    @Column(precision = 5, scale = 2)
    var discountPercentage: BigDecimal? = null,

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    var createdAt: LocalDateTime = LocalDateTime.now()
)