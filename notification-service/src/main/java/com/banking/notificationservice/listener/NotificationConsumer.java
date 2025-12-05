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
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "payment-processed", groupId = "notification-group")
    public void handlePaymentProcessed( PaymentProcessedEvent event ) {
        notificationService.notifyPaymentProcessed(event);
    }

    @KafkaHandler(isDefault = true)
    public void ignoreUnknownEvents(Object event) {}

}
