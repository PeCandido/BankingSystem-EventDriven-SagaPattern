package com.banking.merchantservice.listener;

import com.banking.core.enums.PaymentStatus;
import com.banking.core.event.PaymentCreatedEvent;
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

            merchantService.processReceivedPayment(
                    event.getPayeeId(),
                    event.getAmount()
            );

            log.info("Payment processed successfully for merchant: {}", event.getPayeeId());

        } catch (RuntimeException e) {
            log.error("Error processing payment event: {}", e.getMessage(), e);
        }
    }
}
