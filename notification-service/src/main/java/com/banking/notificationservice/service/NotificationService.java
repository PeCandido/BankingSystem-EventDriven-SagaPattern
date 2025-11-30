package com.banking.notificationservice.service;

import com.banking.core.event.PaymentProcessedEvent;
import com.banking.notificationservice.model.NotificationEntity;
import com.banking.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    public final NotificationRepository notificationRepository;

    public void notifyPaymentProcessed( PaymentProcessedEvent event ) {

        String subject = "Payment updates: " + event.getStatus();
        String content = String.format(
                "Hi, your payment %s has been %s. Details: %s",
                event.getPaymentId(),
                event.getStatus(),
                event.getDescription()
        );

        log.info("==================================");
        log.info("[EMAIL SENT] To: {}", event.getPayerEmail());
        log.info("Subject: {}", subject);
        log.info("Content: {}", content);
        log.info("==================================");

        NotificationEntity notificationEntity = NotificationEntity.builder()
                .paymentId(event.getPaymentId())
                .recipientEmail(event.getPayerEmail())
                .subject(subject)
                .content(content)
                .sentAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notificationEntity);

    }

}
