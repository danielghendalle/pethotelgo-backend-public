package com.api.pethotelgo.service.impl

import com.api.pethotelgo.exception.OwnerNotFoundException
import com.api.pethotelgo.exception.ValidationException
import com.api.pethotelgo.model.dto.CreateOwnerRequest
import com.api.pethotelgo.model.dto.UpdateOwnerRequest
import com.api.pethotelgo.model.dto.OwnerResponse
import com.api.pethotelgo.model.entity.Owner
import com.api.pethotelgo.repository.OwnerRepository
import com.api.pethotelgo.service.OwnerService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class OwnerServiceImpl(private val ownerRepository: OwnerRepository) : OwnerService {

    override fun getAllOwners(): List<Owner> = ownerRepository.findAll()

    override fun getOwnerById(id: String): Owner = ownerRepository.findById(id)
        .orElseThrow { OwnerNotFoundException() }

    override fun createOwner(request: CreateOwnerRequest): Owner {
        val owner = Owner(
            id = UUID.randomUUID().toString(),
            name = request.name,
            phone = request.phone,
            email = request.email,
            createdAt = LocalDateTime.now()
        )
        validateOwnerData(owner)
        return ownerRepository.save(owner)
    }

    override fun toResponse(owner: Owner) = OwnerResponse(
        id = owner.id,
        name = owner.name,
        email = owner.email,
        phone = owner.phone,
        createdAt = owner.createdAt
    )

    override fun updateOwner(id: String, data: UpdateOwnerRequest): Owner {
        val existing = getOwnerById(id)
        existing.name = data.name
        existing.phone = data.phone
        existing.email = data.email
        validateOwnerData(existing)
        return ownerRepository.save(existing)
    }

    override fun deleteOwner(id: String) {
        ownerRepository.delete(getOwnerById(id))
    }

    override fun validateOwnerData(owner: Owner) {
        if (owner.name.isBlank()) throw ValidationException("Owner name is required")
        if (owner.name.length > 100) throw ValidationException("Owner name cannot exceed 100 characters")
        // Email is only mandatory for user login accounts, not for a pet owner
        // record — it's optional here, but must be well-formed when given.
        if (owner.email.isNotBlank() && !EMAIL_REGEX.matches(owner.email)) {
            throw ValidationException("Invalid email format")
        }
        if (owner.phone.isBlank()) throw ValidationException("Owner phone is required")
        if (owner.phone.replace(Regex("[^0-9]"), "").length < 8) {
            throw ValidationException("Phone must have at least 8 digits")
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
