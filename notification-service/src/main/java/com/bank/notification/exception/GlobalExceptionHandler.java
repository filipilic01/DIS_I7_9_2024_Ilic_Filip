package com.bank.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotificationNotFound(NotificationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now()));
    }

    record ErrorResponseDTO(int status, String message, LocalDateTime timestamp) {}
}
