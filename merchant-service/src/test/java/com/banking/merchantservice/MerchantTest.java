package com.banking.merchantservice;

import com.banking.merchantservice.model.Merchant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

public class MerchantTest {

    @Test
    @DisplayName("Should initialize merchant with ID and Balance")
    public void shouldInitializeMerchantWithIDAndBalance() {
        Merchant merchant = new Merchant();
        merchant.initialize(new BigDecimal("100.00"));
        assertThat(merchant.getId()).isNotNull();
        assertThat(merchant.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Should increase balance when receiving valid payment")
    public void shouldIncreaseBalanceWhenReceivingValidPayment() {
        Merchant merchant = Merchant.builder().balance(new BigDecimal("50.00")).build();
        merchant.receivePayment(new BigDecimal("25.50"));
        assertThat(merchant.getBalance()).isEqualByComparingTo(new BigDecimal("75.50"));
    }

    @Test
    @DisplayName("Should throw exception when receiving null payment")
    public void shouldThrowExceptionWhenReceivingNullPayment() {
        Merchant merchant = Merchant.builder().balance(new BigDecimal("50.00")).build();

        assertThatThrownBy(() -> merchant.receivePayment(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when receiving zero or negative payment")
    void shouldThrowExceptionWhenReceivingInvalidPaymentAmount() {
        Merchant merchant = Merchant.builder().balance(new BigDecimal("50.00")).build();

        assertThatThrownBy(() -> merchant.receivePayment(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than zero");

        assertThatThrownBy(() -> merchant.receivePayment(new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    @DisplayName("Should decrease balance when debiting valid amount")
    void shouldDecreaseBalanceWhenDebitingValidAmount() {
        Merchant merchant = Merchant.builder().balance(new BigDecimal("100.00")).build();
        merchant.debitPayment(new BigDecimal("30.00"));

        assertThat(merchant.getBalance()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    @DisplayName("Should throw exception when debit amount is null, zero or negative")
    void shouldThrowExceptionWhenDebitAmountIsInvalid() {
        Merchant merchant = new Merchant();

        assertThatThrownBy(() -> merchant.debitPayment(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> merchant.debitPayment(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than zero");

        assertThatThrownBy(() -> merchant.debitPayment(new BigDecimal("-1.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when balance is insufficient")
    void shouldThrowExceptionWhenInsufficientBalance() {
        Merchant merchant = Merchant.builder().balance(new BigDecimal("50.00")).build();

        assertThatThrownBy(() -> merchant.debitPayment(new BigDecimal("50.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Insufficient balance");
    }

}
