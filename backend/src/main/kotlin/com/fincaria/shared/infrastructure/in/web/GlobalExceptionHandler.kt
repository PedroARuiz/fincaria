package com.fincaria.shared.infrastructure.`in`.web

import com.fincaria.farms.domain.exception.DuplicatedCadastralReferenceException
import com.fincaria.users.domain.exception.EmailAlreadyInUseException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = details,
        )
        return ResponseEntity.badRequest().body(error)
    }
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentErrors(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid request",
        )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(EmailAlreadyInUseException::class)
    fun handleEmailAlreadyInUse(ex: EmailAlreadyInUseException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = ex.message ?: "Email already in use",
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(DuplicatedCadastralReferenceException::class)
    fun handleDuplicatedCadastralReference(ex: DuplicatedCadastralReferenceException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = ex.message ?: "Cadastral reference already in use",
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Request violates a data integrity constraint",
        )
        return ResponseEntity.badRequest().body(error)
    }

}