package com.banking.payment.exception;

public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Exception cause) {
        super(message, cause);
    }
}