package com.api.pethotelgo.controller.api

import com.api.pethotelgo.model.entity.StayHistory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/stay-history")
@Tag(name = "Stay History", description = "Pet stay history and behavior tracking endpoints")
@SecurityRequirement(name = "bearer-jwt")
interface StayHistoryApi {

    @GetMapping
    @Operation(summary = "List all stay histories", description = "Get all pet stay history records")
    fun getAll(): List<StayHistory>

    @GetMapping("/pet/{petId}")
    @Operation(summary = "Get pet's stay history", description = "Get all stay history records for a specific pet")
    fun getByPetPath(@PathVariable petId: String): List<StayHistory>

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create stay history", description = "Record pet behavior and stay information")
    fun create(@RequestBody stayHistory: StayHistory): StayHistory

    @PutMapping("/{id}")
    @Operation(summary = "Update stay history", description = "Update stay history information")
    fun update(@PathVariable id: String, @RequestBody data: StayHistory): StayHistory
}

