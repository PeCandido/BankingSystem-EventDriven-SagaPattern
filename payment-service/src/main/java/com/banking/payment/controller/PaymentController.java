package com.banking.payment.controller;

import com.banking.payment.controller.dto.PaymentDetailsDto;
import com.banking.payment.controller.dto.PaymentDto;
import com.banking.payment.controller.dto.PaymentResponseDto;
import com.banking.payment.model.PaymentEventEntity;
import com.banking.payment.service.PaymentService;
import com.banking.payment.service.PaymentEventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentEventStore paymentEventStore;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(@RequestBody PaymentDto request) {
        UUID paymentId = paymentService.createPayment(request);
        return ResponseEntity.accepted().body(
                new PaymentResponseDto(paymentId, "Payment being processed")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDetailsDto> getPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping
    public ResponseEntity<List<PaymentDetailsDto>> getPayments(@RequestParam(required = false) UUID payerId) {
        if(payerId != null) {
            return ResponseEntity.ok(paymentService.getPaymentsByPayer(payerId));
        }

        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{paymentId}/events")
    public ResponseEntity<List<PaymentEventEntity>> getPaymentHistory(@PathVariable UUID paymentId) {
        List<PaymentEventEntity> events = paymentEventStore.getPaymentHistory(paymentId);
        return ResponseEntity.ok(events);
    }
}