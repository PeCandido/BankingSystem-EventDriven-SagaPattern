package com.banking.notificationservice.notifier;

public interface EmailNotifier {
    void send(String to, String subject, String body);
}
