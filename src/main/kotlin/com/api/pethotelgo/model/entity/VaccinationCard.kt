package com.api.pethotelgo.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "vaccination_cards")
class VaccinationCard(
    @Id
    var id: String = UUID.randomUUID().toString(),

    @Column(name = "pet_id", nullable = false)
    var petId: String = "",

    @Column(name = "file_name", nullable = false)
    var fileName: String = "",

    @Column(name = "file_type")
    var fileType: String? = null,

    @Column(name = "file_data", nullable = false, columnDefinition = "LONGTEXT")
    var fileData: String = "", // base64 encoded file

    @Column(name = "file_size")
    var fileSize: Long? = null,

    @Column(name = "uploaded_at", nullable = false)
    var uploadedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

