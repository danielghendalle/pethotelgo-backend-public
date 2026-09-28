package com.api.pethotelgo.controller.api

import com.api.pethotelgo.model.dto.CreateOwnerRequest
import com.api.pethotelgo.model.dto.OwnerResponse
import com.api.pethotelgo.model.dto.UpdateOwnerRequest
import com.api.pethotelgo.model.entity.Pet
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/owners")
@Tag(name = "Owners", description = "Owner management endpoints")
@SecurityRequirement(name = "bearer-jwt")
interface OwnerApi {

    @GetMapping
    @Operation(summary = "List all owners", description = "Get all pet owners")
    fun getAll(): List<OwnerResponse>

    @GetMapping("/{id}")
    @Operation(summary = "Get owner by ID", description = "Get specific owner information")
    fun getById(@PathVariable id: String): OwnerResponse

    @GetMapping("/{id}/pets")
    @Operation(summary = "Get owner's pets", description = "Get all pets belonging to an owner")
    fun getPetsByOwner(@PathVariable id: String): List<Pet>

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create owner", description = "Create a new pet owner")
    fun create(@RequestBody request: CreateOwnerRequest): OwnerResponse

    @PutMapping("/{id}")
    @Operation(summary = "Update owner", description = "Update owner information")
    fun update(@PathVariable id: String, @RequestBody data: UpdateOwnerRequest): OwnerResponse

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete owner", description = "Delete an owner and associated pets")
    fun delete(@PathVariable id: String)
}
