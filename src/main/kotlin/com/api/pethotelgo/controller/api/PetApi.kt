package com.api.pethotelgo.controller.api

import com.api.pethotelgo.model.dto.CreatePetRequest
import com.api.pethotelgo.model.dto.UpdatePetRequest
import com.api.pethotelgo.model.entity.Pet
import com.api.pethotelgo.model.entity.Reservation
import com.api.pethotelgo.model.entity.StayHistory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/pets")
@Tag(name = "Pets", description = "Pet management endpoints")
@SecurityRequirement(name = "bearer-jwt")
interface PetApi {

    @GetMapping
    @Operation(summary = "List all pets", description = "Get all pets in the system")
    fun getAll(): List<Pet>

    @GetMapping("/{id}")
    @Operation(summary = "Get pet by ID", description = "Get specific pet information")
    fun getById(@PathVariable id: String): Pet

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "List pets by owner", description = "Get all pets of a specific owner")
    fun getByOwnerPath(@PathVariable ownerId: String): List<Pet>

    @GetMapping("/{petId}/reservations")
    @Operation(summary = "Get pet's reservations", description = "Get all reservations for a pet")
    fun getReservationsByPet(@PathVariable petId: String): List<Reservation>

    @GetMapping("/{petId}/stay-history")
    @Operation(summary = "Get pet's stay history", description = "Get all stay history records for a pet")
    fun getStayHistoryByPet(@PathVariable petId: String): List<StayHistory>

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create pet", description = "Register a new pet")
    fun create(@RequestBody request: CreatePetRequest): Pet

    @PutMapping("/{id}")
    @Operation(summary = "Update pet", description = "Update pet information")
    fun update(@PathVariable id: String, @RequestBody data: UpdatePetRequest): Pet

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete pet", description = "Delete a pet")
    fun delete(@PathVariable id: String)
}
