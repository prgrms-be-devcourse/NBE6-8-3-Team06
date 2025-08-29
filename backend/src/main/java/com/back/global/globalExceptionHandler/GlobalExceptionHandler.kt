package com.back.global.globalExceptionHandler

import com.back.global.exception.ServiceException
import com.back.global.rsData.RsData
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(e: ServiceException): ResponseEntity<RsData<Void>> {
        return ResponseEntity.badRequest().body(e.rsData)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handle(ex: NoSuchElementException): ResponseEntity<RsData<Void>> {
        // HTTP 404 Not Found 상태와 함께 에러 응답을 반환합니다.
        return ResponseEntity(
            RsData(
                "404-1",
                ex.message?:"NoSuchElementException"
            ),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handle(ex: IllegalArgumentException): ResponseEntity<RsData<Void>> {
        return ResponseEntity(
            RsData(
                "400-1",
                ex.message?:"IllegalArgumentException"
            ),
            HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(NullPointerException::class)
    fun handle(ex: NullPointerException): ResponseEntity<RsData<Void>> {
        return ResponseEntity(
            RsData(
                "404-1",
                ex.message?:"NullPointerException"
            ),
            HttpStatus.NOT_FOUND
        )
    }
}
