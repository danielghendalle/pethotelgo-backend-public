package com.api.pethotelgo.controller.api

import com.api.pethotelgo.model.dto.AppSettingsResponse
import com.api.pethotelgo.model.dto.UpdateAppSettingsRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/settings")
@Tag(name = "Settings", description = "Global application settings")
@SecurityRequirement(name = "bearer-jwt")
interface SettingsApi {

    @GetMapping
    @Operation(summary = "Get settings", description = "Get the global application settings (boarding daily rates)")
    fun get(): AppSettingsResponse

    @PutMapping
    @Operation(summary = "Update settings", description = "Update the global boarding daily rates")
    fun update(@RequestBody request: UpdateAppSettingsRequest): AppSettingsResponse
}
