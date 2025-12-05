package com.banking.merchantservice.controller;

import com.banking.merchantservice.dto.MerchantDto;
import java.math.BigDecimal;
import com.banking.merchantservice.model.Merchant;
import com.banking.merchantservice.model.MerchantEventEntity;
import com.banking.merchantservice.service.MerchantService;
import com.banking.merchantservice.service.MerchantEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MerchantController {
    private final MerchantService merchantService;
    private final MerchantEventStore merchantEventStore;

    @PostMapping
    public ResponseEntity<Merchant> registerMerchant(@RequestBody MerchantDto request) {
        Merchant merchant = merchantService.registerMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(merchant);
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<Merchant> getMerchant(@PathVariable UUID merchantId) {
        Merchant merchant = merchantService.getMerchant(merchantId);
        return ResponseEntity.ok(merchant);
    }

    @GetMapping
    public ResponseEntity<List<Merchant>> getAllMerchants() {
        List<Merchant> merchants = merchantService.getAllMerchants();
        return ResponseEntity.ok(merchants);
    }

    @GetMapping("/{merchantId}/balance")
    public ResponseEntity<MerchantBalance> getMerchantBalance(@PathVariable UUID merchantId) {
        Merchant merchant = merchantService.getMerchant(merchantId);
        if (merchant != null) {
            return ResponseEntity.ok(new MerchantBalance(merchantId, merchant.getBalance().doubleValue()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{merchantId}/events")
    public ResponseEntity<List<MerchantEventEntity>> getMerchantHistory(@PathVariable UUID merchantId) {
        List<MerchantEventEntity> events = merchantEventStore.getMerchantHistory(merchantId);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/{merchantId}/debit")
    public ResponseEntity<Void> debitMerchant(@PathVariable UUID merchantId, @RequestBody DebitRequest request) {
        merchantService.debitPayer(merchantId, request.getAmount());
        return ResponseEntity.ok().build();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DebitRequest {
        private BigDecimal amount;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MerchantBalance {
        private UUID merchantId;
        private Double balance;
    }
}
