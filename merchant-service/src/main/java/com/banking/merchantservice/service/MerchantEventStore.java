package com.banking.merchantservice.service;

import com.banking.merchantservice.model.MerchantEventEntity;
import com.banking.merchantservice.repository.MerchantEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantEventStore {

    private final MerchantEventRepository merchantEventRepository;

    @Transactional
    public void saveMerchantRegisteredEvent(
            UUID merchantId,
            BigDecimal initialBalance) {

        MerchantEventEntity event = MerchantEventEntity.builder()
                .id(UUID.randomUUID())
                .merchantId(merchantId)
                .balanceChange(initialBalance)
                .newBalance(initialBalance)
                .eventType("MERCHANT_REGISTERED")
                .description("Merchant registered with initial balance")
                .eventDateTime(LocalDateTime.now())
                .build();

        merchantEventRepository.save(event);
        log.info("Merchant event saved: merchantId={}, eventType={}", merchantId, event.getEventType());
    }

    @Transactional
    public void savePaymentReceivedEvent(
            UUID merchantId,
            BigDecimal amount,
            BigDecimal newBalance) {

        MerchantEventEntity event = MerchantEventEntity.builder()
                .id(UUID.randomUUID())
                .merchantId(merchantId)
                .balanceChange(amount)
                .newBalance(newBalance)
                .eventType("PAYMENT_RECEIVED")
                .description("Payment received from customer")
                .eventDateTime(LocalDateTime.now())
                .build();

        merchantEventRepository.save(event);
        log.info("Payment received event saved: merchantId={}, amount={}, newBalance={}",
                merchantId, amount, newBalance);
    }

    public List<MerchantEventEntity> getMerchantHistory(UUID merchantId) {
        return merchantEventRepository.findByMerchantId(merchantId);
    }
}
