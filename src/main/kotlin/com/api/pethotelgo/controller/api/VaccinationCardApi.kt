package com.api.pethotelgo.controller.api

import com.api.pethotelgo.model.dto.VaccinationCardResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/pets/{petId}/vaccination-card")
@Tag(name = "Vaccination Cards", description = "Pet vaccination card management via Google Drive")
@SecurityRequirement(name = "bearer-jwt")
interface VaccinationCardApi {

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Upload vaccination card",
        description = "Upload or replace the vaccination card for a pet (JPEG, PNG, WEBP, PDF — max 10 MB)"
    )
    fun upload(
        @PathVariable petId: String,
        @RequestParam("file") file: MultipartFile
    ): VaccinationCardResponse

    @GetMapping
    @Operation(
        summary = "Get vaccination card",
        description = "Retrieve the vaccination card information for a pet. Uses vaccination_cards first and falls back to pets.vaccinationCardUrl when available; returns 204 when no card data exists"
    )
    fun get(@PathVariable petId: String): ResponseEntity<VaccinationCardResponse>

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete vaccination card",
        description = "Delete the vaccination card for a pet from both the database and Google Drive"
    )
    fun delete(@PathVariable petId: String)
}

