package com.banking.payment.controller;

import com.banking.payment.controller.dto.PaymentDetailsDto;
import com.banking.payment.controller.dto.PaymentDto;
import com.banking.payment.controller.dto.PaymentResponseDto;
import com.banking.payment.model.PaymentEventEntity;
import com.banking.payment.service.PaymentService;
import com.banking.payment.service.PaymentEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentEventStore paymentEventStore;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(@RequestBody PaymentDto request) {
        log.info("💳 Criando novo pagamento: amount={}, payerId={}", request.amount(), request.payerId());
        UUID paymentId = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                new PaymentResponseDto(paymentId, "Payment being processed")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDetailsDto> getPayment(@PathVariable UUID id) {
        log.info("🔍 Buscando pagamento: {}", id);
        PaymentDetailsDto payment = paymentService.getPaymentById(id);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping
    public ResponseEntity<List<PaymentDetailsDto>> getPayments(
            @RequestParam(required = false) UUID payerId
    ) {
        log.info("📊 Listando pagamentos - payerId={}", payerId);

        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{paymentId}/events")
    public ResponseEntity<List<PaymentEventEntity>> getPaymentHistory(@PathVariable UUID paymentId) {
        log.info("📜 Obtendo histórico SAGA do pagamento: {}", paymentId);
        List<PaymentEventEntity> events = paymentEventStore.getPaymentHistory(paymentId);
        return ResponseEntity.ok(events);
    }
}