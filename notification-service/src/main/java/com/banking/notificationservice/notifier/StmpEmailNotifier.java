package com.banking.notificationservice.notifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component("realNotifier")
@Primary
@RequiredArgsConstructor
public class StmpEmailNotifier implements EmailNotifier {

    private final JavaMailSender mailSender;

    @Override
    public void send(String to, String subject, String body) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@banking-system.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Error sending email", e);

        }

    }

}
