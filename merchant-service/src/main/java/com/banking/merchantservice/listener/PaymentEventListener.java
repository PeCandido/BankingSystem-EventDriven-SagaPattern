package com.banking.merchantservice.listener;

import com.banking.core.enums.PaymentStatus;
import com.banking.core.event.PaymentCreatedEvent;
import com.banking.core.event.PaymentProcessedEvent;
import com.banking.merchantservice.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {
    private final MerchantService merchantService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
            topics = "payment-events",
            groupId = "merchant-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCreatedEvent(PaymentCreatedEvent event) {
        log.info("Received PaymentCreatedEvent: {}", event);

        try {
            if (event.getStatus() != PaymentStatus.PENDING) {
                log.warn("Payment is not in PENDING status. Event ignored.");
                return;
            }

            merchantService.getMerchant(event.getPayeeId());
            merchantService.processReceivedPayment(event.getPayeeId(), event.getAmount());

            PaymentProcessedEvent responseEvent = PaymentProcessedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventDateTime(LocalDateTime.now())
                    .paymentId(event.getPaymentId())
                    .payerId(event.getPayerId())
                    .payerEmail("")
                    .status(PaymentStatus.APPROVED)
                    .description("Payment successfully processed by the merchant.")
                    .build();

            kafkaTemplate.send("payment-events", event.getPaymentId().toString(), responseEvent);
            log.info("Payment processed successfully for merchant: {}", responseEvent);

        } catch (RuntimeException e) {
            log.error("Error processing payment event: {}", e.getMessage(), e);

            PaymentProcessedEvent errorEvent = PaymentProcessedEvent.builder()
                    .eventId(java.util.UUID.randomUUID())
                    .eventDateTime(java.time.LocalDateTime.now())
                    .paymentId(event.getPaymentId())
                    .payerId(event.getPayerId())
                    .status(PaymentStatus.REJECTED)
                    .description("Falha no processamento: " + e.getMessage())
                    .build();

            kafkaTemplate.send("payment-events", event.getPaymentId().toString(), errorEvent);
        }
    }
}
