package com.banking.payment.model;

import com.banking.core.enums.PaymentStatus;
import com.banking.payment.exception.InvalidPaymentException;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @NonNull private UUID id;
    @NonNull private UUID payerId;
    @NonNull private String payerEmail;
    @NonNull private UUID payeeId;
    @NonNull private BigDecimal amount;
    @NonNull private String currency;
    @NonNull private PaymentStatus status;

    public void validate(){
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidPaymentException("Amount must be greater than zero");
        }

        if(currency.trim().isEmpty()){
            throw new InvalidPaymentException("Currency cannot be null or empty");
        }
    }

    public void initialize(){
        validate();
        this.status = PaymentStatus.PENDING;
    }

    public void approve(){
        if(this.status != PaymentStatus.PENDING){
            throw new InvalidPaymentException("Only PENDING payment can be approved");
        }

        this.status = PaymentStatus.APPROVED;
    }

    public void reject(){
        if(this.status != PaymentStatus.PENDING){
            throw new InvalidPaymentException("Only PENDING payment can be rejected");
        }
        this.status = PaymentStatus.REJECTED;
    }
}
