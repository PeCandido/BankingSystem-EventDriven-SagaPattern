package com.banking.payment.model;

import com.banking.core.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {
    @Id
    private UUID id;

    private UUID payerId;

    private String payerEmail;

    private UUID payeeId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "BRL";

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
