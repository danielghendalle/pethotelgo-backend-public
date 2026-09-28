package com.api.pethotelgo.service.impl

import com.api.pethotelgo.exception.ValidationException
import com.api.pethotelgo.model.dto.AppSettingsResponse
import com.api.pethotelgo.model.dto.UpdateAppSettingsRequest
import com.api.pethotelgo.model.entity.AppSettings
import com.api.pethotelgo.model.enums.PetSize
import com.api.pethotelgo.repository.AppSettingsRepository
import com.api.pethotelgo.service.AppSettingsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
@Transactional
class AppSettingsServiceImpl(
    private val repository: AppSettingsRepository
) : AppSettingsService {

    @Transactional(readOnly = true)
    override fun getSettings(): AppSettings =
        repository.findById(AppSettings.SINGLETON_ID).orElseGet {
            repository.save(AppSettings(id = AppSettings.SINGLETON_ID))
        }

    override fun updateSettings(request: UpdateAppSettingsRequest): AppSettings {
        validateRate(request.dailyRateStandard, "porte pequeno/médio")
        validateRate(request.dailyRateLarge, "porte grande")

        val settings = getSettings()
        settings.dailyRateStandard = request.dailyRateStandard
        settings.dailyRateLarge = request.dailyRateLarge
        settings.updatedAt = LocalDateTime.now()
        return repository.save(settings)
    }

    @Transactional(readOnly = true)
    override fun dailyRateFor(size: PetSize): BigDecimal {
        val settings = getSettings()
        return if (size == PetSize.grande) settings.dailyRateLarge else settings.dailyRateStandard
    }

    override fun toResponse(settings: AppSettings) = AppSettingsResponse(
        dailyRateStandard = settings.dailyRateStandard,
        dailyRateLarge = settings.dailyRateLarge,
        updatedAt = settings.updatedAt
    )

    private fun validateRate(value: BigDecimal, label: String) {
        if (value <= BigDecimal.ZERO) {
            throw ValidationException("O valor da diária ($label) deve ser maior que zero")
        }
        if (value > BigDecimal("100000")) {
            throw ValidationException("O valor da diária ($label) é muito alto")
        }
    }
}
