package com.banking.payment;

import com.banking.core.enums.PaymentStatus;
import com.banking.payment.exception.PaymentNotFoundException;
import com.banking.payment.model.PaymentEntity;
import com.banking.payment.repository.PaymentRepository;
import com.banking.payment.service.PaymentEventStore;
import com.banking.payment.service.PaymentSaga;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentSagaTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentEventStore paymentEventStore;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private PaymentSaga paymentSaga;

    @Nested
    @DisplayName("Unit tests")
    class UnitTests {

        @Test
        @DisplayName("should execute saga successfully: debit merchant and approve payment")
        void shouldExecuteSagaSuccessfully() {
            UUID paymentId = UUID.randomUUID();
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");

            PaymentEntity payment = PaymentEntity.builder()
                    .id(paymentId)
                    .payerId(payerId)
                    .payeeId(payeeId)
                    .amount(amount)
                    .currency("BRL")
                    .status(PaymentStatus.PENDING)
                    .build();

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.OK));

            paymentSaga.executePaymentSaga(paymentId);

            verify(restTemplate).postForEntity(contains("/debit"), any(), eq(Map.class));

            verify(kafkaTemplate).send(eq("payment-completed"), eq(paymentId.toString()), any());

            verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.APPROVED));
            verify(paymentEventStore).savePaymentApprovedEvent(paymentId);
        }

        @Test
        @DisplayName("should fail saga when merchant debit fails (insufficient funds/error)")
        void shouldFailSagaWhenDebitFails() {
            UUID paymentId = UUID.randomUUID();
            PaymentEntity payment = PaymentEntity.builder()
                    .id(paymentId)
                    .amount(BigDecimal.TEN)
                    .status(PaymentStatus.PENDING)
                    .build();

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

            paymentSaga.executePaymentSaga(paymentId);

            verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.REJECTED));
            verify(paymentEventStore).savePaymentRejectedEvent(paymentId);

            verify(kafkaTemplate, never()).send(eq("payment-completed"), any(), any());
        }

        @Test
        @DisplayName("should propagate exception when repository fails unexpectedly")
        void shouldPropagateExceptionWhenRepositoryFails() {
            UUID paymentId = UUID.randomUUID();
            when(paymentRepository.findById(paymentId)).thenThrow(new RuntimeException("DB Connection Error"));

            assertThrows(RuntimeException.class, () -> paymentSaga.executePaymentSaga(paymentId));

            verifyNoInteractions(restTemplate, kafkaTemplate);
        }

        @Test
        @DisplayName("should fail saga if payment not found initially")
        void shouldFailIfPaymentNotFound() {
            UUID paymentId = UUID.randomUUID();
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

            assertThrows(PaymentNotFoundException.class, () -> {
                paymentSaga.executePaymentSaga(paymentId);
            });
        }

        @Test
        @DisplayName("should fail saga on network timeout or 500 error from Debit Service")
        void shouldFailSagaOnNetworkError() {
            UUID paymentId = UUID.randomUUID();
            PaymentEntity payment = PaymentEntity.builder()
                    .id(paymentId)
                    .status(PaymentStatus.PENDING)
                    .build();

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .thenThrow(new org.springframework.web.client.ResourceAccessException("Connection timed out"));

            try {
                paymentSaga.executePaymentSaga(paymentId);
            } catch (Exception e) {
            }

            verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.REJECTED));
            verify(paymentEventStore).savePaymentRejectedEvent(paymentId);
        }
    }

    @Nested
    @DisplayName("Compensation tests")
    class CompensationTests {

        @Test
        @DisplayName("should trigger compensation (refund payer) when processing fails after debit")
        void shouldTriggerCompensationWhenProcessingFails() {
            UUID paymentId = UUID.randomUUID();
            UUID payerId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");

            PaymentEntity payment = PaymentEntity.builder()
                    .id(paymentId)
                    .payerId(payerId)
                    .amount(amount)
                    .status(PaymentStatus.PENDING)
                    .build();

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            when(restTemplate.postForEntity(contains("/debit"), any(), eq(Map.class)))
                    .thenReturn(new ResponseEntity<>(Map.of("status", "success"), HttpStatus.OK));

            doThrow(new RuntimeException("Database error after debit"))
                    .when(paymentEventStore).savePaymentApprovedEvent(any());

            paymentSaga.executePaymentSaga(paymentId);

            verify(restTemplate).postForEntity(
                    contains("/merchants/" + payerId + "/credit"),
                    any(),
                    eq(Map.class)
            );

            verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.REJECTED));
        }

        @Test
        @DisplayName("Should NOT trigger compensation if debit failed initially")
        void shouldNotTriggerCompensationIfDebitFailed() {
            UUID paymentId = UUID.randomUUID();
            PaymentEntity payment = PaymentEntity.builder()
                    .id(paymentId)
                    .payerId(UUID.randomUUID())
                    .amount(BigDecimal.TEN)
                    .status(PaymentStatus.PENDING)
                    .build();

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            when(restTemplate.postForEntity(contains("/debit"), any(), eq(Map.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

            paymentSaga.executePaymentSaga(paymentId);

            verify(restTemplate, never()).postForEntity(
                    contains("/credit"), any(), eq(Map.class)
            );

            verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.REJECTED));
        }

        @Test
        @DisplayName("Should handle failure during compensation gracefully")
        void shouldHandleCompensationFailure() {
            UUID paymentId = UUID.randomUUID();
            PaymentEntity payment = PaymentEntity.builder()
                    .id(paymentId)
                    .payerId(UUID.randomUUID())
                    .amount(BigDecimal.TEN)
                    .status(PaymentStatus.PENDING)
                    .build();

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            when(restTemplate.postForEntity(contains("/debit"), any(), eq(Map.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.OK));

            doThrow(new RuntimeException("Kafka error"))
                    .when(kafkaTemplate).send(any(), any(), any());

            when(restTemplate.postForEntity(contains("/credit"), any(), eq(Map.class)))
                    .thenThrow(new RuntimeException("Network down during refund"));

            assertDoesNotThrow(() -> paymentSaga.executePaymentSaga(paymentId));

            verify(restTemplate).postForEntity(contains("/credit"), any(), eq(Map.class));

            verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.REJECTED));
        }

    }

}
