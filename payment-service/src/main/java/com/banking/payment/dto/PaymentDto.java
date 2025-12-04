package com.banking.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentDto(
        UUID payerId,
        String payerEmail,
        UUID payeeId,
        BigDecimal amount,
        String currency
) {}
