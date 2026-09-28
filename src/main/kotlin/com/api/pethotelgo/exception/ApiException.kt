package com.api.pethotelgo.exception

open class ApiException(
    val error: ErrorCode,
    message: String? = null,
    val details: Map<String, Any?>? = null
) : RuntimeException(message ?: error.defaultMessage)

open class ValidationException(
    message: String,
    details: Map<String, Any?>? = null
) : ApiException(ErrorCode.VALIDATION_ERROR, message, details)

open class BusinessRuleException(
    message: String,
    details: Map<String, Any?>? = null
) : ApiException(ErrorCode.BUSINESS_RULE_VIOLATION, message, details)

open class ConflictException(
    message: String,
    details: Map<String, Any?>? = null,
    code: ErrorCode = ErrorCode.GENERIC_CONFLICT
) : ApiException(code, message, details)

class OwnerNotFoundException : ApiException(ErrorCode.OWNER_NOT_FOUND)
class PetNotFoundException : ApiException(ErrorCode.PET_NOT_FOUND)
class StayHistoryNotFoundException : ApiException(ErrorCode.STAY_HISTORY_NOT_FOUND)
class ReservationNotFoundException : ApiException(ErrorCode.RESERVATION_NOT_FOUND)
class UserNotFoundException : ApiException(ErrorCode.USER_NOT_FOUND)
class VaccinationCardNotFoundException : ApiException(ErrorCode.VACCINATION_CARD_NOT_FOUND)
