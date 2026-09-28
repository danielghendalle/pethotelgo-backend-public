package com.api.pethotelgo.repository

import com.api.pethotelgo.model.entity.AppSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AppSettingsRepository : JpaRepository<AppSettings, Int>
