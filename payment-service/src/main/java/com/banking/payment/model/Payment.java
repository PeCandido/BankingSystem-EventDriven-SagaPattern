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

    private UUID id;
    private UUID payerId;
    private String payerEmail;
    private UUID payeeId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;

    public void validate(){
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidPaymentException("Amount must be greater than zero");
        }

        if(currency == null || currency.trim().isEmpty()){
            throw new InvalidPaymentException("Currency cannot be null or empty");
        }

        if(payerId == null){
            throw new InvalidPaymentException("Payer ID cannot be null or empty");
        }

        if(payeeId == null){
            throw new InvalidPaymentException("Payee id is null");
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
