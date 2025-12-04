package com.banking.merchantservice.listener;

import com.banking.core.event.PaymentCompletedEvent;
import com.banking.merchantservice.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {
    private final MerchantService merchantService;

    @KafkaListener(
            topics = "payment-completed",
            groupId = "merchant-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("💰 [MERCHANT] Received PaymentCompletedEvent: paymentId={}, payeeId={}, amount={}",
                event.getPaymentId(), event.getPayeeId(), event.getAmount());

        try {
            merchantService.processReceivedPayment(event.getPayeeId(), event.getAmount());
            log.info("✅ Merchant {} credited with {} successfully",
                    event.getPayeeId(), event.getAmount());
        } catch (Exception e) {
            log.error("❌ Error crediting merchant: {}", e.getMessage(), e);
        }
    }
}
