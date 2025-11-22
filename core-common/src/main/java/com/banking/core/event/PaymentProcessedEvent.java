package com.banking.core.event;

import com.banking.core.enums.PaymentStatus;

import java.util.UUID;

public class PaymentProcessedEvent extends BaseEvent{
    private UUID paymentId;
    private UUID payerId;
    private String payerEmail;
    private PaymentStatus status;
    private String description;
}
