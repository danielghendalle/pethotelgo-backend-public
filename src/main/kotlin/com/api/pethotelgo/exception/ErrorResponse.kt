package com.api.pethotelgo.exception

import java.time.Instant

data class ErrorResponse(
    val timestamp: Instant = Instant.now(),
    val path: String? = null,
    val code: String,
    val status: Int,
    val error: String,
    val message: String,
    val details: Map<String, Any?>? = null
)

