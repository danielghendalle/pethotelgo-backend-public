package com.api.pethotelgo.model.dto

import com.api.pethotelgo.model.entity.Reservation
import java.time.LocalDate

data class DayCapacity(
    var date: LocalDate,
    var totalPets: Int,
    var isFull: Boolean,
    var reservations: List<Reservation>
)