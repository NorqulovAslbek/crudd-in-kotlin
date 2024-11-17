package com.example.crudd.exp


import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandlerController {
    @ExceptionHandler(AppBadException::class)
    private fun handle(e: AppBadException): ResponseEntity<Any> {
        return ResponseEntity.badRequest().body(e.message)
    }

    @ExceptionHandler(ForbiddenException::class)
    private fun handle(e: ForbiddenException): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }


    @ExceptionHandler(RuntimeException::class)
    private fun handle(e: RuntimeException): ResponseEntity<Any> {
        return ResponseEntity.internalServerError().body(e.message)
    }
}

class AppBadException(message: String) : RuntimeException(message)

class ForbiddenException(message: String) : RuntimeException(message)
