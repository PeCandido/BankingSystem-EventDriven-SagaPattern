package com.banking.payment.service;

import com.banking.core.event.PaymentCreatedEvent;
import com.banking.payment.controller.dto.PaymentDetailsDto;
import com.banking.payment.controller.dto.PaymentDto;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public UUID createPayment(PaymentDto request) {
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
        log.info("Payment saved with id {}", paymentEntity.getId());

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

        kafkaTemplate.send("payment-events", payment.getId().toString(), createdEvent);
        log.info("Event sent to Kafka: {}", createdEvent);

        return payment.getId();
    }

    public PaymentDetailsDto getPaymentById(UUID id) {
        return paymentRepository.findById(id)
                .map(PaymentDetailsDto::fromEntity)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with id " + id + " not found"));
    }

    public List<PaymentDetailsDto> getPaymentsByPayer(UUID payerId) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getPayerId().equals(payerId))
                .map(PaymentDetailsDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approvedPayment(UUID id) {
        PaymentEntity entity = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        Payment payment = PaymentMapper.toDomain(entity);
        payment.approve();

        entity.setStatus(payment.getStatus());
        paymentRepository.save(entity);

        log.info("Payment APPROVED with id {}", payment.getId());
    }

    @Transactional
    public void rejectPayment(UUID id) {
        PaymentEntity entity = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        Payment payment = PaymentMapper.toDomain(entity);
        payment.reject();

        entity.setStatus(payment.getStatus());
        paymentRepository.save(entity);

        log.info("Payment REJECTED with id {}", payment.getId());
    }
}
