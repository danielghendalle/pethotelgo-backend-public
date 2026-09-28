package com.api.pethotelgo.service

import com.api.pethotelgo.model.dto.CreateOwnerRequest
import com.api.pethotelgo.model.dto.OwnerResponse
import com.api.pethotelgo.model.dto.UpdateOwnerRequest
import com.api.pethotelgo.model.entity.Owner

interface OwnerService {
    fun getAllOwners(): List<Owner>
    fun getOwnerById(id: String): Owner
    fun createOwner(request: CreateOwnerRequest): Owner
    fun updateOwner(id: String, data: UpdateOwnerRequest): Owner
    fun deleteOwner(id: String)
    fun validateOwnerData(owner: Owner)
    fun toResponse(owner: Owner): OwnerResponse
}

