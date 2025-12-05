package com.banking.merchantservice.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Merchant {
    @NonNull private UUID id;
    @NonNull private String name;
    @NonNull private String email;
    @NonNull private String phone;
    @NonNull private BigDecimal balance;
    @NonNull private String currency;

    public void receivePayment(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        this.balance = this.balance.add(amount);
    }

    public void debitPayment(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void initialize(BigDecimal initialBalance) {
        this.id = UUID.randomUUID();
        this.balance = initialBalance;
    }
}
