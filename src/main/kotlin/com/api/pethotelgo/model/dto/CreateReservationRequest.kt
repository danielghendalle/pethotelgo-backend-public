package com.api.pethotelgo.model.dto

import com.api.pethotelgo.model.enums.ReservationStatus
import java.time.LocalDateTime
import java.math.BigDecimal

data class CreateReservationRequest(
    val petId: String,
    val ownerId: String,
    val checkIn: LocalDateTime,
    val checkOut: LocalDateTime,
    val status: ReservationStatus = ReservationStatus.pending,
    val notes: String = "",
    val dailyRate: BigDecimal? = null,
    val discountPercentage: BigDecimal? = null
)
