package com.api.pethotelgo.controller

import com.api.pethotelgo.controller.api.OwnerApi
import com.api.pethotelgo.model.dto.CreateOwnerRequest
import com.api.pethotelgo.model.dto.OwnerResponse
import com.api.pethotelgo.model.entity.Owner
import com.api.pethotelgo.model.dto.UpdateOwnerRequest
import com.api.pethotelgo.model.entity.Pet
import com.api.pethotelgo.service.OwnerService
import org.springframework.web.bind.annotation.*

@RestController
class OwnerController(private val ownerService: OwnerService) : OwnerApi {

    override fun getAll(): List<OwnerResponse> = ownerService.getAllOwners().map { ownerService.toResponse(it) }

    override fun getById(id: String): OwnerResponse = ownerService.toResponse(ownerService.getOwnerById(id))

    override fun getPetsByOwner(id: String): List<Pet> = ownerService.getOwnerById(id).pets

    override fun create(request: CreateOwnerRequest): OwnerResponse = ownerService.toResponse(ownerService.createOwner(request))

    override fun update(id: String, data: UpdateOwnerRequest): OwnerResponse = ownerService.toResponse(ownerService.updateOwner(id, data))

    override fun delete(id: String) = ownerService.deleteOwner(id)
}
