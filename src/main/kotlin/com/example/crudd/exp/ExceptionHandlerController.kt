package com.example.crudd.exp

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler



import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = ex.bindingResult.allErrors.associate {
            val fieldName = (it as FieldError).field
            fieldName to (it.defaultMessage ?: "Invalid value")
        }
        val errorResponse = mapOf(
            "code" to "VALIDATION_ERROR",
            "message" to errors
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }
}

@ControllerAdvice
class ExceptionHandlerController {

    private val log = LoggerFactory.getLogger(ExceptionHandlerController::class.java)

    @ExceptionHandler(AppBadException::class)
    fun handleAppBadException(e: AppBadException): ResponseEntity<*> {
        log.error(e.toString())
        return ResponseEntity.badRequest().body(e.message)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ResponseEntity<*> {
        log.error(e.toString())
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build<Any>()
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<*> {
        log.error(e.toString())
        return ResponseEntity.internalServerError().body(e.message)
    }
}

class AppBadException(message: String) : RuntimeException(message)

class ForbiddenException(message: String):RuntimeException(message)