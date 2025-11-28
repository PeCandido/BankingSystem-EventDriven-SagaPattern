package com.banking.payment.controller.dto;

import java.util.UUID;

public record PaymentResponseDto(UUID paymentId, String message) {}
