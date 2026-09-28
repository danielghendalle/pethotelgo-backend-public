package com.api.pethotelgo.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val code: String, val status: HttpStatus, val defaultMessage: String) {
    // Not Found
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "Resource not found"),
    OWNER_NOT_FOUND("OWNER_NOT_FOUND", HttpStatus.NOT_FOUND, "Owner not found"),
    PET_NOT_FOUND("PET_NOT_FOUND", HttpStatus.NOT_FOUND, "Pet not found"),
    STAY_HISTORY_NOT_FOUND("STAY_HISTORY_NOT_FOUND", HttpStatus.NOT_FOUND, "Stay history not found"),
    RESERVATION_NOT_FOUND("RESERVATION_NOT_FOUND", HttpStatus.NOT_FOUND, "Reservation not found"),
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"),
    VACCINATION_CARD_NOT_FOUND("VACCINATION_CARD_NOT_FOUND", HttpStatus.NOT_FOUND, "Vaccination card not found"),

    // Validation / Business
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation error"),
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", HttpStatus.BAD_REQUEST, "Business rule violated"),
    PAYLOAD_TOO_LARGE("PAYLOAD_TOO_LARGE", HttpStatus.PAYLOAD_TOO_LARGE, "Uploaded file is too large"),

    // Conflicts
    GENERIC_CONFLICT("CONFLICT", HttpStatus.CONFLICT, "Conflict"),
    RESERVATION_CONFLICT("RESERVATION_CONFLICT", HttpStatus.CONFLICT, "Reservation conflict"),
    CAPACITY_FULL("CAPACITY_FULL", HttpStatus.CONFLICT, "Capacity full"),

    // Auth
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "Forbidden"),

    // Generic
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")
}
