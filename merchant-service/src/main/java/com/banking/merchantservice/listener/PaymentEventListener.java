package com.banking.merchantservice.listener;

import com.banking.core.event.PaymentProcessedEvent;
import com.banking.merchantservice.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {
    private final MerchantService merchantService;

    @KafkaListener(
            topics = "payment-processed",
            groupId = "merchant-service-group"
    )
    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("💰 [MERCHANT] Recebido PaymentProcessedEvent: paymentId={}, payerId={}, payeeId={}, amount={}, status={}",
                event.getPaymentId(), event.getPayerId(), event.getPayeeId(), event.getAmount(), event.getStatus());

        try {
            if ("APPROVED".equals(event.getStatus())) {
                merchantService.debitPayer(event.getPayerId(), event.getAmount());

                merchantService.processReceivedPayment(event.getPayeeId(), event.getAmount());


            } else if ("REJECTED".equals(event.getStatus())) {
                log.info("⚠️ Pagamento {} foi rejeitado: {}", event.getPaymentId(), event.getDescription());
            }
        } catch (Exception e) {
            log.error("❌ Erro ao processar pagamento {}: {}", event.getPaymentId(), e.getMessage(), e);
        }
    }
}
