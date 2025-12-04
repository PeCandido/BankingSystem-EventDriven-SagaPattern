package com.banking.payment.model;

import com.banking.core.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID paymentId;

    @Column(nullable = false)
    private UUID payerId;

    @Column(nullable = false)
    private UUID payeeId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime eventDateTime;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
