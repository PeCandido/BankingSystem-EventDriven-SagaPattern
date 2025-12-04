package com.banking.payment.service;

import com.banking.core.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaListener {
    private final PaymentSaga paymentSaga;

    @KafkaListener(
            topics = "payment-created",
            groupId = "payment-saga-group"
    )
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        log.info("🎯 [LISTENER] Recebido payment-created: {}", event.getPaymentId());
        paymentSaga.executePaymentSaga(event.getPaymentId());
    }
}
