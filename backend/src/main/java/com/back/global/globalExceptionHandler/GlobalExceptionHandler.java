package com.back.global.globalExceptionHandler;

import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<Void>> handleServiceException(ServiceException e){
        return ResponseEntity.badRequest().body(e.getRsData());
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException ex) {
        // HTTP 404 Not Found 상태와 함께 에러 응답을 반환합니다.
        return new ResponseEntity<>(
                new RsData<>(
                        "404-1",
                        ex.getMessage()
                ),
                NOT_FOUND
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RsData<Void>> handle(IllegalArgumentException ex) {
        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        ex.getMessage()
                ),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<RsData<Void>> handle(NullPointerException ex) {
        return new ResponseEntity<>(
                new RsData<>(
                        "404-1",
                        "NullPointerException"
                ),
                NOT_FOUND
        );
    }
}
