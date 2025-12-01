package com.banking.notificationservice.service;

import com.banking.core.event.PaymentProcessedEvent;
import com.banking.notificationservice.model.NotificationEntity;
import com.banking.notificationservice.notifier.EmailNotifier;
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
    private final EmailNotifier emailNotifier;

    public void notifyPaymentProcessed( PaymentProcessedEvent event ) {

        String toEmail = event.getPayerEmail();
        if( toEmail == null ) {
            toEmail = "admin@banking.com";
        }

        String subject = "Payment update: " + event.getStatus();
        String content = String.format(
                "Hi %s, \n\nYour payment ID %s has been processed. \nStatus: %s\nReason: %s",
                event.getPayerEmail(),
                event.getPaymentId(),
                event.getStatus(),
                event.getDescription()
        );

        emailNotifier.send(toEmail, subject, content);

        NotificationEntity notificationEntity = NotificationEntity.builder()
                .paymentId(event.getPaymentId())
                .recipientEmail(toEmail)
                .subject(subject)
                .content(content)
                .sentAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notificationEntity);
        log.info("Notification saved");
    }

}
