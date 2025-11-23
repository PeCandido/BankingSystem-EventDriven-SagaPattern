package com.banking.payment.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentDto(
        UUID payerId,
        UUID payeeId,
        BigDecimal amount,
        String currency
){}
