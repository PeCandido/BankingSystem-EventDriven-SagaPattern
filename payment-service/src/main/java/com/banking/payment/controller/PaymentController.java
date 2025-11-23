package com.banking.payment.controller;

import com.banking.payment.controller.dto.PaymentDto;
import com.banking.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<String> createPayment(@RequestBody PaymentDto request) {
        paymentService.createPayment(request);
        return ResponseEntity.accepted().body("Payment being processed");
    }
}
