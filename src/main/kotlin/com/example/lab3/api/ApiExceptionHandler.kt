package com.example.lab3.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

data class ApiError(
    val status: Int,
    val error: String,
    val message: String
)

class NotFoundException(message: String) : RuntimeException(message)

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ApiError> {
        return buildError(HttpStatus.NOT_FOUND, ex.message ?: "Not found")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val message = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Validation failed"
        return buildError(HttpStatus.BAD_REQUEST, message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleBadRequestBody(): ResponseEntity<ApiError> {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid request body")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiError> {
        return buildError(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(): ResponseEntity<ApiError> {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid request parameter")
    }

    private fun buildError(status: HttpStatus, message: String): ResponseEntity<ApiError> {
        return ResponseEntity.status(status).body(
            ApiError(
                status = status.value(),
                error = status.reasonPhrase,
                message = message
            )
        )
    }
}
