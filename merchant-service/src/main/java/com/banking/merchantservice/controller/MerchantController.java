package com.banking.merchantservice.controller;

import com.banking.merchantservice.controller.dto.MerchantDto;
import com.banking.merchantservice.model.Merchant;
import com.banking.merchantservice.model.MerchantEventEntity;  // ✅ Novo import
import com.banking.merchantservice.service.MerchantService;
import com.banking.merchantservice.service.MerchantEventStore;  // ✅ Novo import
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;  // ✅ Novo import
import java.util.UUID;

@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
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

    @GetMapping("/email/{email}")
    public ResponseEntity<Merchant> getMerchantByEmail(@PathVariable String email) {
        Merchant merchant = merchantService.getMerchantByEmail(email);
        return ResponseEntity.ok(merchant);
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Merchant> getMerchantByPhone(@PathVariable String phone) {
        Merchant merchant = merchantService.getMerchantByPhone(phone);
        return ResponseEntity.ok(merchant);
    }

    @GetMapping("/{merchantId}/events")
    public ResponseEntity<List<MerchantEventEntity>> getMerchantHistory(@PathVariable UUID merchantId) {
        List<MerchantEventEntity> events = merchantEventStore.getMerchantHistory(merchantId);
        return ResponseEntity.ok(events);
    }
}
