package com.banking.notificationservice.notifier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("fakeNotifier")
public class ConsoleEmailNotifier implements EmailNotifier {

    @Override
    public void send(String to, String subject, String body) {
        log.info("================[CONSOLE EMAIL]================");
        log.info("TO: {}", to);
        log.info("SUBJECT: {}", subject);
        log.info("BODY: {}", body);
        log.info("===============================================");
    }
}
