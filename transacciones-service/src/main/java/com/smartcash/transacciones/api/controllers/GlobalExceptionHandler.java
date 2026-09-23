package com.smartcash.transacciones.api.controllers;

import com.smartcash.transacciones.domain.exceptions.TransaccionInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransaccionInvalidaException.class)
    public ResponseEntity<Map<String, String>> handleTransaccionInvalida(TransaccionInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
