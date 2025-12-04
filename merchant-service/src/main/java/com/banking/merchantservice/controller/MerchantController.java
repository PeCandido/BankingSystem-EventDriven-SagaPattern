package com.banking.merchantservice.controller;

import com.banking.merchantservice.dto.MerchantDto;
import com.banking.merchantservice.model.Merchant;
import com.banking.merchantservice.model.MerchantEventEntity;
import com.banking.merchantservice.service.MerchantService;
import com.banking.merchantservice.service.MerchantEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * MerchantController - API corrigida
 */
@Slf4j
@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MerchantController {
    private final MerchantService merchantService;
    private final MerchantEventStore merchantEventStore;

    /**
     * Registrar novo merchant
     */
    @PostMapping
    public ResponseEntity<Merchant> registerMerchant(@RequestBody MerchantDto request) {
        log.info("📝 Registrando merchant: {}", request.email());
        Merchant merchant = merchantService.registerMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(merchant);
    }

    /**
     * Obter merchant por ID
     */
    @GetMapping("/{merchantId}")
    public ResponseEntity<Merchant> getMerchant(@PathVariable UUID merchantId) {
        log.info("🔍 Buscando merchant: {}", merchantId);
        Merchant merchant = merchantService.getMerchant(merchantId);
        return ResponseEntity.ok(merchant);
    }

    /**
     * Listar merchants
     */
    @GetMapping
    public ResponseEntity<List<Merchant>> getAllMerchants() {
        log.info("📊 Listando merchants");
        return ResponseEntity.ok(List.of());
    }

    /**
     * Obter saldo do merchant
     */
    @GetMapping("/{merchantId}/balance")
    public ResponseEntity<MerchantBalance> getMerchantBalance(@PathVariable UUID merchantId) {
        log.info("💰 Saldo merchant: {}", merchantId);
        Merchant merchant = merchantService.getMerchant(merchantId);
        if (merchant != null) {
            return ResponseEntity.ok(new MerchantBalance(merchantId, merchant.getBalance().doubleValue()));  // ✅ BigDecimal → Double
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Histórico de eventos
     */
    @GetMapping("/{merchantId}/events")
    public ResponseEntity<List<MerchantEventEntity>> getMerchantHistory(@PathVariable UUID merchantId) {
        log.info("📜 Histórico merchant: {}", merchantId);
        List<MerchantEventEntity> events = merchantEventStore.getMerchantHistory(merchantId);
        return ResponseEntity.ok(events);
    }

    /**
     * DTO para saldo
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MerchantBalance {
        private UUID merchantId;
        private Double balance;
    }
}
