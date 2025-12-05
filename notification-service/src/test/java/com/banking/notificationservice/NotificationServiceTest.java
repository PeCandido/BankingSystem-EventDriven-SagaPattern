package com.banking.notificationservice;

import com.banking.core.event.PaymentProcessedEvent;
import com.banking.notificationservice.interfaces.UnitTest;
import com.banking.notificationservice.model.NotificationEntity;
import com.banking.notificationservice.notifier.EmailNotifier;
import com.banking.notificationservice.repository.NotificationRepository;
import com.banking.notificationservice.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock private EmailNotifier emailNotifier;

    @InjectMocks
    private NotificationService notificationService;

    @Nested
    @DisplayName("Unit tests")
    class UnitTests {

        @UnitTest
        @DisplayName("Should send email and save notification when payer email is present")
        void shouldNotifyPaymentProcessed_WithValidEmail() {
            UUID paymentId = UUID.randomUUID();
            String payerEmail = "user@test.com";
            String status = "COMPLETED";
            String description = "Payment successful";

            PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                    .paymentId(paymentId)
                    .payerEmail(payerEmail)
                    .status(status)
                    .description(description)
                    .amount(new BigDecimal("100.00"))
                    .build();

            notificationService.notifyPaymentProcessed(event);

            verify(emailNotifier).send(eq(payerEmail), any(String.class), any(String.class));

            ArgumentCaptor<NotificationEntity> entityCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
            verify(notificationRepository).save(entityCaptor.capture());

            NotificationEntity savedEntity = entityCaptor.getValue();
            assertEquals(paymentId, savedEntity.getPaymentId());
            assertEquals(payerEmail, savedEntity.getRecipientEmail());
            assertTrue(savedEntity.getContent().contains(paymentId.toString()));
            assertTrue(savedEntity.getSubject().contains(status));
            assertNotNull(savedEntity.getSentAt());
        }

        @UnitTest
        @DisplayName("Should send to admin email when payer email is null")
        void shouldNotifyPaymentProcessed_WhenEmailIsNull_FallbackToAdmin() {

            UUID paymentId = UUID.randomUUID();
            String status = "FAILED";

            PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                    .paymentId(paymentId)
                    .payerEmail(null)
                    .status(status)
                    .description("Insufficient funds")
                    .build();

            notificationService.notifyPaymentProcessed(event);

            verify(emailNotifier).send(eq("admin@banking.com"), any(String.class), any(String.class));

            ArgumentCaptor<NotificationEntity> entityCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
            verify(notificationRepository).save(entityCaptor.capture());

            NotificationEntity savedEntity = entityCaptor.getValue();
            assertEquals("admin@banking.com", savedEntity.getRecipientEmail());
            assertEquals(paymentId, savedEntity.getPaymentId());
        }

        @UnitTest
        @DisplayName("Should NOT save notification when email sending fails")
        void shouldNotSaveNotification_WhenEmailNotifierFails() {
            PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                    .paymentId(UUID.randomUUID())
                    .payerEmail("user@test.com")
                    .status("COMPLETED")
                    .description("Success")
                    .build();

            doThrow(new RuntimeException("Email server down"))
                    .when(emailNotifier).send(any(), any(), any());

            assertThrows(RuntimeException.class, () -> notificationService.notifyPaymentProcessed(event));

            verify(notificationRepository, never()).save(any());
        }

        @UnitTest
        @DisplayName("Should propagate exception when repository save fails")
        void shouldPropagateException_WhenRepositoryFails() {
            PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                    .paymentId(UUID.randomUUID())
                    .payerEmail("user@test.com")
                    .status("COMPLETED")
                    .build();

            doNothing().when(emailNotifier).send(any(), any(), any());
            doThrow(new RuntimeException("Database connection error"))
                    .when(notificationRepository).save(any());

            assertThrows(RuntimeException.class, () -> notificationService.notifyPaymentProcessed(event));

            verify(emailNotifier).send(any(), any(), any());
        }

        @UnitTest
        @DisplayName("Should throw NPE when event is null")
        void shouldThrowNPE_WhenEventIsNull() {
            assertThrows(NullPointerException.class, () -> notificationService.notifyPaymentProcessed(null));
        }

        @UnitTest
        @DisplayName("Should format email content correctly")
        void shouldFormatEmailContentCorrectly() {

            UUID paymentId = UUID.randomUUID();
            String status = "FAILED";
            String description = "Insufficient funds";
            String email = "client@test.com";

            PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                    .paymentId(paymentId)
                    .payerEmail(email)
                    .status(status)
                    .description(description)
                    .build();

            notificationService.notifyPaymentProcessed(event);

            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailNotifier).send(eq(email), any(), contentCaptor.capture());

            String sentContent = contentCaptor.getValue();

            assertTrue(sentContent.contains(paymentId.toString()), "Email body should contain Payment ID");
            assertTrue(sentContent.contains(status), "Email body should contain Status");
            assertTrue(sentContent.contains(description), "Email body should contain Reason/Description");
        }

        @UnitTest
        @DisplayName("Should respect execution order: Email first, then Save")
        void shouldRespectExecutionOrder() {
            PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                    .paymentId(UUID.randomUUID())
                    .payerEmail("order@test.com")
                    .status("COMPLETED")
                    .build();

            notificationService.notifyPaymentProcessed(event);
            org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(emailNotifier, notificationRepository);

            inOrder.verify(emailNotifier).send(any(), any(), any());

            inOrder.verify(notificationRepository).save(any());
        }

        @UnitTest
        @DisplayName("Should attempt to send to empty email if provided (Current Behavior)")
        void shouldSendToEmptyString_WhenEmailIsBlank() {
            String emptyEmail = "";
            PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                    .paymentId(UUID.randomUUID())
                    .payerEmail(emptyEmail)
                    .status("PENDING")
                    .build();

            notificationService.notifyPaymentProcessed(event);

            verify(emailNotifier).send(eq(""), any(), any());
            verify(emailNotifier, never()).send(eq("admin@banking.com"), any(), any());
        }

    }

}
