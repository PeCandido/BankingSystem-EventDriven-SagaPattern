package com.banking.payment.saga;

import com.banking.core.event.PaymentCreatedEvent;
import com.banking.core.event.PaymentProcessedEvent;
import com.banking.payment.service.PaymentService;
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
public class PaymentSagaOrchestrator {

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "payment-created", groupId = "saga-orchestrator-group")
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        log.info("🎭 Saga iniciada para payment: {}", event.getPaymentId());

        try {
            paymentService.approvedPayment(event.getPaymentId());
            log.info("✅ Payment aprovado");

            PaymentProcessedEvent processedEvent = PaymentProcessedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventDateTime(LocalDateTime.now())
                    .paymentId(event.getPaymentId())
                    .payerId(event.getPayerId())
                    .payeeId(event.getPayeeId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .status("APPROVED")
                    .payerEmail(event.getPayerEmail())
                    .description("Pagamento processado com sucesso")
                    .build();

            kafkaTemplate.send("payment-processed", event.getPaymentId().toString(), processedEvent);
            log.info("📤 Evento payment-processed publicado");

        } catch (Exception e) {
            log.error("❌ Erro na saga", e);

            PaymentProcessedEvent rejectedEvent = PaymentProcessedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventDateTime(LocalDateTime.now())
                    .paymentId(event.getPaymentId())
                    .payerId(event.getPayerId())
                    .payeeId(event.getPayeeId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .status("REJECTED")
                    .payerEmail(event.getPayerEmail())
                    .description("Erro ao processar: " + e.getMessage())
                    .build();

            kafkaTemplate.send("payment-processed", event.getPaymentId().toString(), rejectedEvent);
        }
    }
}
