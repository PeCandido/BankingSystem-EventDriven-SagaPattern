package com.banking.payment.listener;

import com.banking.core.enums.PaymentStatus;
import com.banking.core.event.PaymentProcessedEvent;
import com.banking.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentResponseListener {
    private final PaymentService paymentService;

    @KafkaListener(topics = "payment-processed", groupId = "payment-service-group")
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("Processed payment event: {}", event);

        if (event.getStatus() == PaymentStatus.APPROVED)
            paymentService.approvedPayment(event.getPaymentId());
        else paymentService.rejectPayment(event.getPaymentId());
    }

    @KafkaHandler(isDefault = true)
    public void ignoreUnknownEvents(Object event) {}

}
