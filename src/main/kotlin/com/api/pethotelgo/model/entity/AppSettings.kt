package com.api.pethotelgo.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Global application settings. Always a single row with [id] = 1.
 */
@Entity
@Table(name = "app_settings")
class AppSettings(
    @Id
    var id: Int = SINGLETON_ID,

    /** Daily boarding rate for small and medium pets. */
    @Column(name = "daily_rate_standard", nullable = false, precision = 10, scale = 2)
    var dailyRateStandard: BigDecimal = BigDecimal("50.00"),

    /** Daily boarding rate for large pets. */
    @Column(name = "daily_rate_large", nullable = false, precision = 10, scale = 2)
    var dailyRateLarge: BigDecimal = BigDecimal("80.00"),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
