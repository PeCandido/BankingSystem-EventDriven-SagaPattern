package com.banking.payment;

import com.banking.core.enums.PaymentStatus;
import com.banking.payment.dto.PaymentDetailsDto;
import com.banking.payment.dto.PaymentDto;
import com.banking.payment.exception.InvalidPaymentException;
import com.banking.payment.exception.PaymentNotFoundException;
import com.banking.payment.model.PaymentEntity;
import com.banking.payment.repository.PaymentRepository;
import com.banking.payment.service.PaymentEventStore;
import com.banking.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentEventStore paymentEventStore;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks private PaymentService paymentService;

    @Nested
    @DisplayName("Unit tests")
    class UnitTests {

        @UnitTest
        @DisplayName("Should create payment successfully when data is valid")
        void shouldCreatePaymentSuccessfully() {
            PaymentDto request = new PaymentDto(
                    UUID.randomUUID(),
                    "payer@test.com",
                    UUID.randomUUID(),
                    new BigDecimal("100.50"),
                    "BRL"
            );

            when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(i -> i.getArgument(0));

            UUID paymentId = paymentService.createPayment(request);

            assertNotNull(paymentId);

            verify(paymentRepository).save(argThat(p ->
                    p.getStatus() == PaymentStatus.PENDING &&
                            p.getAmount().equals(new BigDecimal("100.50")) &&
                            p.getCurrency().equals("BRL")
            ));

            verify(paymentEventStore).savePaymentCreatedEvent(
                    eq(paymentId),
                    eq(request.payerId()),
                    eq(request.payeeId()),
                    eq(request.amount()),
                    eq("BRL"),
                    eq(PaymentStatus.PENDING)
            );

            verify(kafkaTemplate).send(eq("payment-created"), eq(paymentId.toString()), any());
        }

        @UnitTest
        @DisplayName("should throw exception when amount is zero or negative")
        void shouldThrowExceptionForInvalidAmount() {
            PaymentDto zeroAmount = new PaymentDto(UUID.randomUUID(), "test", UUID.randomUUID(), BigDecimal.ZERO, "BRL");
            PaymentDto negativeAmount = new PaymentDto(UUID.randomUUID(), "test", UUID.randomUUID(), new BigDecimal("-10"), "BRL");

            assertThrows(InvalidPaymentException.class, () -> paymentService.createPayment(zeroAmount));
            assertThrows(InvalidPaymentException.class, () -> paymentService.createPayment(negativeAmount));

            verifyNoInteractions(paymentRepository, kafkaTemplate);
        }

        @UnitTest
        @DisplayName("should throw exception when currency is missing")
        void shouldThrowExceptionForMissingCurrency() {
            PaymentDto request = new PaymentDto(UUID.randomUUID(), "test", UUID.randomUUID(), BigDecimal.TEN, "");

            assertThrows(InvalidPaymentException.class, () -> paymentService.createPayment(request));
            verifyNoInteractions(paymentRepository);
        }

        @UnitTest
        @DisplayName("should throw exception when IDs are missing")
        void shouldThrowExceptionForMissingIds() {
            PaymentDto noPayer = new PaymentDto(null, "test", UUID.randomUUID(), BigDecimal.TEN, "BRL");
            PaymentDto noPayee = new PaymentDto(UUID.randomUUID(), "test", null, BigDecimal.TEN, "BRL");

            assertThrows(InvalidPaymentException.class, () -> paymentService.createPayment(noPayer));
            assertThrows(InvalidPaymentException.class, () -> paymentService.createPayment(noPayee));
        }

        @UnitTest
        @DisplayName("should return payment details when found")
        void shouldReturnPaymentById() {
            UUID id = UUID.randomUUID();
            PaymentEntity entity = new PaymentEntity(id, UUID.randomUUID(), "email", UUID.randomUUID(), BigDecimal.TEN, "BRL", PaymentStatus.PENDING);

            when(paymentRepository.findById(id)).thenReturn(Optional.of(entity));

            PaymentDetailsDto result = paymentService.getPaymentById(id);

            assertNotNull(result);
            assertEquals(id, result.id());
            assertEquals(BigDecimal.TEN, result.amount());
        }

        @UnitTest
        @DisplayName("should throw exception when payment not found")
        void shouldThrowExceptionWhenPaymentNotFound() {
            UUID id = UUID.randomUUID();
            when(paymentRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentById(id));
        }

        @UnitTest
        @DisplayName("should return all payments")
        void shouldReturnAllPayments() {
            when(paymentRepository.findAll()).thenReturn(List.of(
                    new PaymentEntity(), new PaymentEntity()
            ));

            List<PaymentDetailsDto> result = paymentService.getAllPayments();

            assertEquals(2, result.size());
        }

        @UnitTest
        @DisplayName("should filter payments by payer id")
        void shouldFilterPaymentsByPayer() {
            UUID payer1 = UUID.randomUUID();
            UUID payer2 = UUID.randomUUID();

            PaymentEntity p1 = PaymentEntity.builder().payerId(payer1).build();
            PaymentEntity p2 = PaymentEntity.builder().payerId(payer2).build();

            when(paymentRepository.findAll()).thenReturn(List.of(p1, p2));

            List<PaymentDetailsDto> result = paymentService.getPaymentsByPayer(payer1);

            assertEquals(1, result.size());
            assertEquals(payer1, result.getFirst().payerId());
        }

        @UnitTest
        @DisplayName("should approve pending payment successfully")
        void shouldApprovePayment() {
            UUID id = UUID.randomUUID();
            PaymentEntity entity = PaymentEntity.builder().id(id).status(PaymentStatus.PENDING).build();

            when(paymentRepository.findById(id)).thenReturn(Optional.of(entity));

            paymentService.approvedPayment(id);

            assertEquals(PaymentStatus.APPROVED, entity.getStatus());
            verify(paymentRepository).save(entity);
            verify(paymentEventStore).savePaymentApprovedEvent(id);
        }

        @UnitTest
        @DisplayName("should throw exception when approving non-existent payment")
        void shouldThrowExceptionWhenApprovingUnknown() {
            UUID id = UUID.randomUUID();
            when(paymentRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(NoSuchElementException.class, () -> paymentService.approvedPayment(id));

            verify(paymentRepository, never()).save(any());
        }

        @UnitTest
        @DisplayName("should throw exception when trying to approve already processed payment")
        void shouldThrowExceptionWhenApprovingNonPending() {
            UUID id = UUID.randomUUID();
            PaymentEntity approvedEntity = PaymentEntity.builder().id(id).status(PaymentStatus.APPROVED).build();
            PaymentEntity rejectedEntity = PaymentEntity.builder().id(id).status(PaymentStatus.REJECTED).build();

            when(paymentRepository.findById(id)).thenReturn(Optional.of(approvedEntity));
            assertThrows(InvalidPaymentException.class, () -> paymentService.approvedPayment(id));

            when(paymentRepository.findById(id)).thenReturn(Optional.of(rejectedEntity));
            assertThrows(InvalidPaymentException.class, () -> paymentService.approvedPayment(id));
        }

        @UnitTest
        @DisplayName("should reject pending payment successfully")
        void shouldRejectPayment() {
            UUID id = UUID.randomUUID();
            PaymentEntity entity = PaymentEntity.builder().id(id).status(PaymentStatus.PENDING).build();

            when(paymentRepository.findById(id)).thenReturn(Optional.of(entity));

            paymentService.rejectPayment(id);

            assertEquals(PaymentStatus.REJECTED, entity.getStatus());
            verify(paymentRepository).save(entity);
            verify(paymentEventStore).savePaymentRejectedEvent(id);
        }

        @UnitTest
        @DisplayName("should throw exception when rejecting non-existent payment")
        void shouldThrowExceptionWhenRejectingUnknown() {
            UUID id = UUID.randomUUID();
            when(paymentRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(PaymentNotFoundException.class, () -> paymentService.rejectPayment(id));
        }

        @UnitTest
        @DisplayName("should throw exception when trying to reject already processed payment")
        void shouldThrowExceptionWhenRejectingNonPending() {
            UUID id = UUID.randomUUID();
            PaymentEntity entity = PaymentEntity.builder().id(id).status(PaymentStatus.APPROVED).build();

            when(paymentRepository.findById(id)).thenReturn(Optional.of(entity));

            assertThrows(InvalidPaymentException.class, () -> paymentService.rejectPayment(id));

            verify(paymentEventStore, never()).savePaymentRejectedEvent(any());
        }

        @UnitTest
        @DisplayName("should throw exception when payer and payee are the same")
        void shouldThrowExceptionForSelfPayment() {
            UUID sameId = UUID.randomUUID();
            PaymentDto request = new PaymentDto(
                    sameId, "payer@test.com", sameId, new BigDecimal("100.00"), "BRL"
            );

            assertThrows(InvalidPaymentException.class, () -> paymentService.createPayment(request));
            verifyNoInteractions(paymentRepository);
        }

        @UnitTest
        @DisplayName("should propagate exception when Kafka fails to send event")
        void shouldThrowExceptionWhenKafkaFails() {
            PaymentDto request = new PaymentDto(
                    UUID.randomUUID(), "test", UUID.randomUUID(), BigDecimal.TEN, "BRL"
            );

            when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            when(kafkaTemplate.send(anyString(), anyString(), any()))
                    .thenThrow(new RuntimeException("Kafka is down"));

            assertThrows(RuntimeException.class, () -> paymentService.createPayment(request));

            verify(paymentRepository).save(any());
        }

        @UnitTest
        @DisplayName("should throw exception when amount has more than 2 decimal places")
        void shouldThrowExceptionForInvalidScale() {
            PaymentDto request = new PaymentDto(
                    UUID.randomUUID(), "test", UUID.randomUUID(), new BigDecimal("100.559"), "BRL"
            );

            assertThrows(InvalidPaymentException.class, () -> paymentService.createPayment(request));
        }

    }



}
