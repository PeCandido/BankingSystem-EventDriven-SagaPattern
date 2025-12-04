package com.banking.payment.controller.dto;

import com.banking.core.enums.PaymentStatus;
import com.banking.payment.model.PaymentEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentDetailsDto(
        UUID id,
        UUID payerId,
        UUID payeeId,
        BigDecimal amount,
        String currency,
        PaymentStatus status
) {
    public static PaymentDetailsDto fromEntity(PaymentEntity entity) {
        return new PaymentDetailsDto(
                entity.getId(),
                entity.getPayerId(),
                entity.getPayeeId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus()
        );
    }
}
