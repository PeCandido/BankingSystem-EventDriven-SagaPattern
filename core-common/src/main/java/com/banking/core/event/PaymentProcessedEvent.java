package com.banking.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private UUID eventId;
    private LocalDateTime eventDateTime;
    private UUID paymentId;
    private UUID payerId;
    private UUID payeeId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String payerEmail;
    private String description;
}
