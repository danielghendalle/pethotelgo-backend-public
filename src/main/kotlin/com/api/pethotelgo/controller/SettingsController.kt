package com.api.pethotelgo.controller

import com.api.pethotelgo.controller.api.SettingsApi
import com.api.pethotelgo.model.dto.AppSettingsResponse
import com.api.pethotelgo.model.dto.UpdateAppSettingsRequest
import com.api.pethotelgo.service.AppSettingsService
import org.springframework.web.bind.annotation.RestController

@RestController
class SettingsController(private val settingsService: AppSettingsService) : SettingsApi {

    override fun get(): AppSettingsResponse =
        settingsService.toResponse(settingsService.getSettings())

    override fun update(request: UpdateAppSettingsRequest): AppSettingsResponse =
        settingsService.toResponse(settingsService.updateSettings(request))
}
