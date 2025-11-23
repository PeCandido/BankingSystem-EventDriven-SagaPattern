package com.banking.payment.mapper;

import com.banking.payment.model.Payment;
import com.banking.payment.model.PaymentEntity;

public class PaymentMapper {
    public static PaymentEntity toEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId())
                .payerId(payment.getPayerId())
                .payeeId(payment.getPayeeId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .build();
    }

    public static Payment toDomain(PaymentEntity entity) {
        if(entity == null) return null;

        return new Payment(
                entity.getId(),
                entity.getPayerId(),
                entity.getPayeeId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus()
        );
    }

}
