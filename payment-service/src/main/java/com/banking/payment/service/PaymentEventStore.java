package com.banking.payment.service;

import com.banking.core.enums.PaymentStatus;
import com.banking.payment.model.PaymentEntity;
import com.banking.payment.model.PaymentEventEntity;
import com.banking.payment.repository.PaymentEventRepository;
import com.banking.payment.repository.PaymentRepository;
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
public class PaymentEventStore {

    private final PaymentEventRepository paymentEventRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void savePaymentCreatedEvent(
            UUID paymentId,
            UUID payerId,
            UUID payeeId,
            BigDecimal amount,
            String currency,
            PaymentStatus status
    ) {
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

    @Transactional
    public void savePaymentApprovedEvent(UUID paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        PaymentEventEntity event = PaymentEventEntity.builder()
                .id(UUID.randomUUID())
                .paymentId(paymentId)
                .payerId(payment.getPayerId())
                .payeeId(payment.getPayeeId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(PaymentStatus.APPROVED)
                .eventType("PAYMENT_APPROVED")
                .eventDateTime(LocalDateTime.now())
                .build();

        paymentEventRepository.save(event);
        log.info("Payment APPROVED event saved: paymentId={}", paymentId);
    }

    @Transactional
    public void savePaymentRejectedEvent(UUID paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        PaymentEventEntity event = PaymentEventEntity.builder()
                .id(UUID.randomUUID())
                .paymentId(paymentId)
                .payerId(payment.getPayerId())
                .payeeId(payment.getPayeeId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(PaymentStatus.REJECTED)
                .eventType("PAYMENT_REJECTED")
                .eventDateTime(LocalDateTime.now())
                .build();

        paymentEventRepository.save(event);
        log.info("Payment REJECTED event saved: paymentId={}", paymentId);
    }

    public List<PaymentEventEntity> getPaymentHistory(UUID paymentId) {
        return paymentEventRepository.findByPaymentId(paymentId);
    }

    public List<PaymentEventEntity> getPayerPaymentHistory(UUID payerId) {
        return paymentEventRepository.findByPayerId(payerId);
    }

    public List<PaymentEventEntity> getPayeePaymentHistory(UUID payeeId) {
        return paymentEventRepository.findByPayeeId(payeeId);
    }
}
