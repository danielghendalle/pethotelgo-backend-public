package com.api.pethotelgo.service

import com.api.pethotelgo.model.dto.AppSettingsResponse
import com.api.pethotelgo.model.dto.UpdateAppSettingsRequest
import com.api.pethotelgo.model.entity.AppSettings
import com.api.pethotelgo.model.enums.PetSize
import java.math.BigDecimal

interface AppSettingsService {
    fun getSettings(): AppSettings
    fun updateSettings(request: UpdateAppSettingsRequest): AppSettings

    /** Default daily boarding rate for a pet of the given size. */
    fun dailyRateFor(size: PetSize): BigDecimal

    fun toResponse(settings: AppSettings): AppSettingsResponse
}
