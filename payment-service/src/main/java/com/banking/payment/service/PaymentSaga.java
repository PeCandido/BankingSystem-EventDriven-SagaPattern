package com.banking.payment.service;

import com.banking.core.enums.PaymentStatus;
import com.banking.core.event.PaymentCompletedEvent;
import com.banking.payment.exception.PaymentNotFoundException;
import com.banking.payment.exception.PaymentProcessingException;
import com.banking.payment.model.PaymentEntity;
import com.banking.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSaga {
    private final PaymentRepository paymentRepository;
    private final PaymentEventStore paymentEventStore;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Transactional
    public void executePaymentSaga(UUID paymentId) {
        log.info("🔄 ═══════════════════════════════════════════");
        log.info("🔄 [SAGA START] Payment ID: {}", paymentId);
        log.info("🔄 ═══════════════════════════════════════════");

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        log.info("💳 [SAGA] Payer: {}", payment.getPayerId());
        log.info("💳 [SAGA] Payee: {}", payment.getPayeeId());
        log.info("💳 [SAGA] Amount: {}", payment.getAmount());
        log.info("💳 [SAGA] Currency: {}", payment.getCurrency());

        try {
            log.info("📤 [SAGA STEP 1] Debitando payer: {}", payment.getPayerId());
            boolean debitSuccess = callMerchantServiceDebit(
                    payment.getPayerId(),
                    payment.getAmount()
            );

            if (!debitSuccess) {
                log.error("❌ Falha ao debitar payer");
                failSaga(payment, "Debit failed - insufficient funds or connection error");
                return;
            }

            log.info("✅ [SAGA STEP 1] Débito realizado com sucesso");

            log.info("📤 [SAGA STEP 2] Publicando PaymentCompletedEvent");
            publishPaymentCompletedEvent(payment);
            log.info("✅ [SAGA STEP 2] PaymentCompletedEvent publicado");

            payment.setStatus(PaymentStatus.APPROVED);
            paymentRepository.save(payment);
            paymentEventStore.savePaymentApprovedEvent(paymentId);

            log.info("✅ [SAGA COMPLETA] Payment {} - APPROVED", paymentId);

        } catch (Exception e) {
            log.error("❌ [SAGA FAILED] {}", e.getMessage(), e);

            try {
                log.warn("♻️ [COMPENSATION] Tentando estornar crédito payee: {}", payment.getPayeeId());
                boolean compensationSuccess = callMerchantServiceDebit(payment.getPayeeId(), payment.getAmount());
                log.info("♻️ Compensation result: {}", compensationSuccess ? "SUCCESS" : "FAILED");
            } catch (Exception compEx) {
                log.error("❌ Falha na compensação payee", compEx);
            }

            failSaga(payment, e.getMessage());
        }
    }

    private boolean callMerchantServiceDebit(UUID merchantId, BigDecimal amount) {
        try {
            String url = "http://merchant-service:8082/api/merchants/" + merchantId + "/debit";

            log.info("🌐 ═══════════════════════════════════════════");
            log.info("🌐 [DEBIT CALL] URL: {}", url);
            log.info("🌐 [DEBIT CALL] MerchantID: {}", merchantId);
            log.info("🌐 [DEBIT CALL] Amount: {}", amount);
            log.info("🌐 ═══════════════════════════════════════════");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("amount", amount);

            log.info("📤 [DEBIT REQUEST BODY] {}", body);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            log.info("📥 [DEBIT RESPONSE] Status: {}", response.getStatusCode());
            log.info("📥 [DEBIT RESPONSE] Body: {}", response.getBody());
            log.info("📥 [DEBIT RESPONSE] Headers: {}", response.getHeaders());

            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("✅ [DEBIT RESULT] Success: {}", success);

            return success;

        } catch (Exception e) {
            log.error("❌ ═══════════════════════════════════════════");
            log.error("❌ [DEBIT ERROR] Exception Type: {}", e.getClass().getSimpleName());
            log.error("❌ [DEBIT ERROR] Message: {}", e.getMessage());
            log.error("❌ [DEBIT ERROR] URL Attempted: {}",
                    "http://merchant-service:8082/api/merchants/" + merchantId + "/debit");
            log.error("❌ ═══════════════════════════════════════════", e);

            return false;
        }
    }

    private void publishPaymentCompletedEvent(PaymentEntity payment) {
        try {
            PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventDateTime(LocalDateTime.now())
                    .paymentId(payment.getId())
                    .payerId(payment.getPayerId())
                    .payerEmail("")
                    .payeeId(payment.getPayeeId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .status(PaymentStatus.APPROVED)
                    .build();

            kafkaTemplate.send("payment-completed", payment.getId().toString(), event);
            log.info("📤 Kafka PaymentCompletedEvent enviado: paymentId={}, payeeId={}, amount={}",
                    payment.getId(), payment.getPayeeId(), payment.getAmount());

        } catch (Exception e) {
            log.error("❌ Erro ao publicar PaymentCompletedEvent:", e);
            throw new PaymentProcessingException("Kafka publish failed", e);
        }
    }

    private void failSaga(PaymentEntity payment, String reason) {
        try {
            payment.setStatus(PaymentStatus.REJECTED);
            paymentRepository.save(payment);
            paymentEventStore.savePaymentRejectedEvent(payment.getId());
            log.error("❌ [SAGA REJECTED] Payment {} | Reason: {}", payment.getId(), reason);
        } catch (Exception e) {
            log.error("❌ Erro ao falhar saga:", e);
        }
    }
}
