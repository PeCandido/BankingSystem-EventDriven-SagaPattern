package com.banking.merchantservice.controller;

import com.banking.merchantservice.exception.MerchantNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class MerchantControllerAdvice {

    @ExceptionHandler(MerchantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMerchantNotFound(MerchantNotFoundException e) {
        ErrorResponse error = new ErrorResponse("MERCHANT_NOT_FOUND", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    public record ErrorResponse(String code, String message) {}
}
