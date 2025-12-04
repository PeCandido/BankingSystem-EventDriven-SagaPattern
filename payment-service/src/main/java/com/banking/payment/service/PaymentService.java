package com.banking.payment.service;

import com.banking.core.event.PaymentCreatedEvent;
import com.banking.payment.dto.PaymentDetailsDto;
import com.banking.payment.dto.PaymentDto;
import com.banking.payment.exception.PaymentNotFoundException;
import com.banking.payment.mapper.PaymentMapper;
import com.banking.payment.model.Payment;
import com.banking.payment.model.PaymentEntity;
import com.banking.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventStore paymentEventStore;
    private final PaymentSaga paymentSaga;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public UUID createPayment(PaymentDto request) {
        log.info("💳 Criando payment: {} → {}", request.payerId(), request.payeeId());

        Payment payment = new Payment(
                null,
                request.payerId(),
                request.payerEmail(),
                request.payeeId(),
                request.amount(),
                request.currency(),
                null
        );

        payment.initialize();
        payment.setId(UUID.randomUUID());

        PaymentEntity paymentEntity = PaymentMapper.toEntity(payment);
        paymentRepository.save(paymentEntity);
        log.info("💾 Payment salvo com id: {}", paymentEntity.getId());

        paymentEventStore.savePaymentCreatedEvent(
                paymentEntity.getId(),
                payment.getPayerId(),
                payment.getPayeeId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus()
        );

        PaymentCreatedEvent createdEvent = PaymentCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventDateTime(LocalDateTime.now())
                .paymentId(payment.getId())
                .payerId(payment.getPayerId())
                .payeeId(payment.getPayeeId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .payerEmail(payment.getPayerEmail())
                .build();

        kafkaTemplate.send("payment-created", payment.getId().toString(), createdEvent);
        log.info("📤 Kafka event sent: payment-created");

        return payment.getId();
    }

    public PaymentDetailsDto getPaymentById(UUID id) {
        log.info("🔍 Buscando payment: {}", id);
        return paymentRepository.findById(id)
                .map(PaymentDetailsDto::fromEntity)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + id));
    }

    public List<PaymentDetailsDto> getPaymentsByPayer(UUID payerId) {
        log.info("📊 Pagamentos do payer: {}", payerId);
        return paymentRepository.findAll().stream()
                .filter(p -> p.getPayerId().equals(payerId))
                .map(PaymentDetailsDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PaymentDetailsDto> getAllPayments() {
        log.info("📊 Listando todos os pagamentos");
        return paymentRepository.findAll().stream()
                .map(PaymentDetailsDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approvedPayment(UUID id) {
        log.info("✅ Aprovando payment: {}", id);
        PaymentEntity entity = paymentRepository.findById(id).orElseThrow();

        Payment payment = PaymentMapper.toDomain(entity);
        payment.approve();

        entity.setStatus(payment.getStatus());
        paymentRepository.save(entity);

        paymentEventStore.savePaymentApprovedEvent(id);
    }

    @Transactional
    public void rejectPayment(UUID id) {
        log.info("❌ Rejeitando payment: {}", id);
        PaymentEntity entity = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        Payment payment = PaymentMapper.toDomain(entity);
        payment.reject();

        entity.setStatus(payment.getStatus());
        paymentRepository.save(entity);

        paymentEventStore.savePaymentRejectedEvent(id);
    }
}