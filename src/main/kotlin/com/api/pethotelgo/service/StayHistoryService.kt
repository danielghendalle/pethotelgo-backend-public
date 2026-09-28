package com.api.pethotelgo.service

import com.api.pethotelgo.model.entity.StayHistory

interface StayHistoryService {
    fun getAllStayHistories(): List<StayHistory>
    fun getStayHistoriesByPetId(petId: String): List<StayHistory>
    fun createStayHistory(stayHistory: StayHistory): StayHistory
    fun updateStayHistory(id: String, data: StayHistory): StayHistory
    fun validateStayHistoryData(stayHistory: StayHistory)
}

