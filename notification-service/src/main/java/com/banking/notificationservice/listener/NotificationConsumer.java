package com.banking.notificationservice.listener;

import com.banking.core.event.PaymentProcessedEvent;
import com.banking.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@KafkaListener(topics = "payment-events", groupId = "notification-group")
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaHandler
    public void handlePaymentProcessed( PaymentProcessedEvent event ) {
        log.info("Received payment processed event: {}", event);
        notificationService.notifyPaymentProcessed(event);
    }

    @KafkaHandler(isDefault = true)
    public void ignoreUnknownEvents(Object event) {}

}
