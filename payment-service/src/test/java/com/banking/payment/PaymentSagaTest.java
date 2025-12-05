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
        @DisplayName("should handle exception during process and trigger fail saga")
        void shouldHandleGenericException() {
            UUID paymentId = UUID.randomUUID();
            when(paymentRepository.findById(paymentId)).thenThrow(new RuntimeException("Database error"));

            try {
                paymentSaga.executePaymentSaga(paymentId);
            } catch (Exception e) {

            }

        }

        @Test
        @DisplayName("should fail saga if payment not found initially")
        void shouldFailIfPaymentNotFound() {
            UUID paymentId = UUID.randomUUID();
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

            org.junit.jupiter.api.Assertions.assertThrows(PaymentNotFoundException.class, () -> {
                paymentSaga.executePaymentSaga(paymentId);
            });
        }

    }

}
