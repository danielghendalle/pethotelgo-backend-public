package com.api.pethotelgo.service

import com.api.pethotelgo.model.dto.CreateReservationRequest
import com.api.pethotelgo.model.entity.Reservation
import com.api.pethotelgo.model.dto.DayCapacity
import java.time.LocalDate

interface ReservationService {
    fun getAllReservations(): List<Reservation>
    fun getReservationById(id: String): Reservation
    fun getReservationsByDate(date: LocalDate): List<Reservation>
    fun getReservationsByPetId(petId: String): List<Reservation>
    fun createReservation(request: CreateReservationRequest): Reservation
    fun updateReservation(id: String, data: Reservation): Reservation
    fun deleteReservation(id: String)
    fun updateReservationStatus(id: String, newStatus: String): Reservation
    fun validateReservationData(reservation: Reservation, isUpdate: Boolean = false)
    fun checkDayCapacity(date: LocalDate): DayCapacity
}

