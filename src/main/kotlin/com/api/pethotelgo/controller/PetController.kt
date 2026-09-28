package com.api.pethotelgo.controller

import com.api.pethotelgo.controller.api.PetApi
import com.api.pethotelgo.model.dto.CreatePetRequest
import com.api.pethotelgo.model.entity.Pet
import com.api.pethotelgo.model.dto.UpdatePetRequest
import com.api.pethotelgo.model.entity.Reservation
import com.api.pethotelgo.model.entity.StayHistory
import com.api.pethotelgo.service.PetService
import com.api.pethotelgo.service.ReservationService
import com.api.pethotelgo.service.StayHistoryService
import org.springframework.web.bind.annotation.*

@RestController
class PetController(
    private val petService: PetService,
    private val reservationService: ReservationService,
    private val stayHistoryService: StayHistoryService
) : PetApi {

    override fun getAll(): List<Pet> = petService.getAllPets()

    override fun getById(id: String): Pet = petService.getPetById(id)

    override fun getByOwnerPath(ownerId: String): List<Pet> = petService.getPetsByOwnerId(ownerId)

    override fun getReservationsByPet(petId: String): List<Reservation> =
        reservationService.getReservationsByPetId(petId)

    override fun getStayHistoryByPet(petId: String): List<StayHistory> =
        stayHistoryService.getStayHistoriesByPetId(petId)

    override fun create(request: CreatePetRequest): Pet = petService.createPet(request)

    override fun update(id: String, data: UpdatePetRequest): Pet =
        petService.updatePet(id, data)

    override fun delete(id: String) = petService.deletePet(id)
}
