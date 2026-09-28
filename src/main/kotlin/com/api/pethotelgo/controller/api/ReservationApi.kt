package com.api.pethotelgo.controller.api

import com.api.pethotelgo.model.dto.CreateReservationRequest
import com.api.pethotelgo.model.entity.Reservation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RequestMapping("/reservations")
@Tag(name = "Reservations", description = "Pet reservation management endpoints")
@SecurityRequirement(name = "bearer-jwt")
interface ReservationApi {

    @GetMapping
    @Operation(
        summary = "List reservations",
        description = "Get all reservations, optionally filtered by date"
    )
    fun getAll(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?
    ): List<Reservation>

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Get specific reservation details")
    fun getById(@PathVariable id: String): Reservation

    @GetMapping("/pet/{petId}")
    @Operation(summary = "Get pet's reservations", description = "Get all reservations for a specific pet")
    fun getByPetPath(@PathVariable petId: String): List<Reservation>

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create reservation", description = "Create a new pet reservation")
    fun create(@RequestBody request: CreateReservationRequest): Reservation

    @PutMapping("/{id}")
    @Operation(summary = "Update reservation", description = "Update reservation details")
    fun update(@PathVariable id: String, @RequestBody data: Reservation): Reservation

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete reservation", description = "Delete a pending reservation")
    fun delete(@PathVariable id: String)

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update reservation status", description = "Change reservation status (pending → confirmed/cancelled → completed/cancelled)")
    fun updateStatus(@PathVariable id: String, @RequestBody body: Map<String, String>): Reservation
}

