package com.banking.payment.dto;

import java.util.UUID;

public record PaymentResponseDto(
        UUID paymentId,
        String message
) {}
