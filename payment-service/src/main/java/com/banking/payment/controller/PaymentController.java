package com.banking.payment.controller;

import com.banking.payment.dto.PaymentDetailsDto;
import com.banking.payment.dto.PaymentDto;
import com.banking.payment.dto.PaymentResponseDto;
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
        log.info("💳 POST /payments - Criando pagamento");
        UUID paymentId = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                new PaymentResponseDto(paymentId, "Payment being processed by SAGA")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDetailsDto> getPayment(@PathVariable UUID id) {
        log.info("🔍 GET /payments/{} - Buscando pagamento", id);
        PaymentDetailsDto payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping
    public ResponseEntity<List<PaymentDetailsDto>> getPayments(
            @RequestParam(required = false) UUID payerId
    ) {
        log.info("📊 GET /payments - Listando pagamentos");

        if (payerId != null) {
            return ResponseEntity.ok(paymentService.getPaymentsByPayer(payerId));
        }
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{paymentId}/events")
    public ResponseEntity<List<PaymentEventEntity>> getPaymentHistory(@PathVariable UUID paymentId) {
        log.info("📜 GET /payments/{}/events - Obtendo histórico SAGA", paymentId);
        List<PaymentEventEntity> events = paymentEventStore.getPaymentHistory(paymentId);
        return ResponseEntity.ok(events);
    }
}