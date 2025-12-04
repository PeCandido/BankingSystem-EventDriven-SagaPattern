package com.banking.core.event;

import com.banking.core.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class PaymentCompletedEvent extends BaseEvent {
    private UUID paymentId;
    private UUID payerId;
    private String payerEmail;
    private UUID payeeId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
}
