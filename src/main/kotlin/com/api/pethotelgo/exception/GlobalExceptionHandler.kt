package com.api.pethotelgo.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.server.ResponseStatusException
import org.springframework.security.core.userdetails.UsernameNotFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val error = ex.error
        val body = ErrorResponse(
            path = request.requestURI,
            code = error.code,
            status = error.status.value(),
            error = error.status.reasonPhrase,
            message = ex.message ?: error.defaultMessage,
            details = ex.details
        )
        return ResponseEntity.status(error.status).body(body)
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFound(ex: UsernameNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val error = ErrorCode.USER_NOT_FOUND
        val body = ErrorResponse(
            path = request.requestURI,
            code = error.code,
            status = error.status.value(),
            error = error.status.reasonPhrase,
            message = ex.message ?: error.defaultMessage
        )
        return ResponseEntity.status(error.status).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val error = ErrorCode.VALIDATION_ERROR
        val fieldErrors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        val body = ErrorResponse(
            path = request.requestURI,
            code = error.code,
            status = error.status.value(),
            error = error.status.reasonPhrase,
            message = error.defaultMessage,
            details = mapOf("fields" to fieldErrors)
        )
        return ResponseEntity.status(error.status).body(body)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val status = ex.statusCode
        val body = ErrorResponse(
            path = request.requestURI,
            code = when (status.value()) {
                400 -> ErrorCode.VALIDATION_ERROR.code
                401 -> ErrorCode.UNAUTHORIZED.code
                403 -> ErrorCode.FORBIDDEN.code
                404 -> ErrorCode.NOT_FOUND.code
                409 -> ErrorCode.GENERIC_CONFLICT.code
                else -> ErrorCode.INTERNAL_ERROR.code
            },
            status = status.value(),
            error = ex.titleMessageCode,
            message = ex.reason.toString()
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(ex: HttpMessageNotReadableException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val error = ErrorCode.VALIDATION_ERROR
        val body = ErrorResponse(
            path = request.requestURI,
            code = error.code,
            status = error.status.value(),
            error = error.status.reasonPhrase,
            message = "Malformed or incomplete request body"
        )
        return ResponseEntity.status(error.status).body(body)
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSize(ex: MaxUploadSizeExceededException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val error = ErrorCode.PAYLOAD_TOO_LARGE
        val body = ErrorResponse(
            path = request.requestURI,
            code = error.code,
            status = error.status.value(),
            error = error.status.reasonPhrase,
            message = error.defaultMessage
        )
        return ResponseEntity.status(error.status).body(body)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val error = ErrorCode.VALIDATION_ERROR
        val body = ErrorResponse(
            path = request.requestURI,
            code = error.code,
            status = error.status.value(),
            error = error.status.reasonPhrase,
            message = ex.message ?: error.defaultMessage
        )
        return ResponseEntity.status(error.status).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception for {} {}", request.method, request.requestURI, ex)
        val error = ErrorCode.INTERNAL_ERROR
        val body = ErrorResponse(
            path = request.requestURI,
            code = error.code,
            status = error.status.value(),
            error = error.status.reasonPhrase,
            message = error.defaultMessage
        )
        return ResponseEntity.status(error.status).body(body)
    }
}
