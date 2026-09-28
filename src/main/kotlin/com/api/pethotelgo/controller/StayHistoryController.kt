package com.api.pethotelgo.controller

import com.api.pethotelgo.controller.api.StayHistoryApi
import com.api.pethotelgo.model.entity.StayHistory
import com.api.pethotelgo.service.StayHistoryService
import org.springframework.web.bind.annotation.RestController

@RestController
class StayHistoryController(private val stayHistoryService: StayHistoryService) : StayHistoryApi {

    override fun getAll(): List<StayHistory> = stayHistoryService.getAllStayHistories()

    override fun getByPetPath(petId: String): List<StayHistory> =
        stayHistoryService.getStayHistoriesByPetId(petId)

    override fun create(stayHistory: StayHistory): StayHistory =
        stayHistoryService.createStayHistory(stayHistory)

    override fun update(id: String, data: StayHistory): StayHistory =
        stayHistoryService.updateStayHistory(id, data)
}
