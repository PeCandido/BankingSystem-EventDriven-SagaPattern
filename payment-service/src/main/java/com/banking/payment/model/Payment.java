package com.banking.payment.model;

import com.banking.core.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    private UUID id;
    private UUID payerId;
    private UUID payeeId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;

    public void initialize(){
        this.status = PaymentStatus.PENDING;
    }
}
