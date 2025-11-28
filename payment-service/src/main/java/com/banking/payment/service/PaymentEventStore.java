package com.banking.payment.service;

import com.banking.payment.model.PaymentEventEntity;
import com.banking.payment.repository.PaymentEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import com.banking.core.enums.PaymentStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventStore {

    private final PaymentEventRepository paymentEventRepository;

    @Transactional
    public void savePaymentCreatedEvent(
            UUID paymentId,
            UUID payerId,
            UUID payeeId,
            BigDecimal amount,
            String currency,
            PaymentStatus status) {

        PaymentEventEntity event = PaymentEventEntity.builder()
                .id(UUID.randomUUID())
                .paymentId(paymentId)
                .payerId(payerId)
                .payeeId(payeeId)
                .amount(amount)
                .currency(currency)
                .status(status)
                .eventType("PAYMENT_CREATED")
                .eventDateTime(LocalDateTime.now())
                .build();

        paymentEventRepository.save(event);
        log.info("Payment event saved: paymentId={}, eventType={}", paymentId, event.getEventType());
    }

    public List<PaymentEventEntity> getPaymentHistory(UUID paymentId) {
        return paymentEventRepository.findByPaymentId(paymentId);
    }

    public List<PaymentEventEntity> getMerchantPaymentHistory(UUID payeeId) {
        return paymentEventRepository.findByPayeeId(payeeId);
    }
}
