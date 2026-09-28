package com.api.pethotelgo.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDateTime

data class AppSettingsResponse(
    val dailyRateStandard: BigDecimal,
    val dailyRateLarge: BigDecimal,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    val updatedAt: LocalDateTime
)

data class UpdateAppSettingsRequest(
    val dailyRateStandard: BigDecimal,
    val dailyRateLarge: BigDecimal
)
