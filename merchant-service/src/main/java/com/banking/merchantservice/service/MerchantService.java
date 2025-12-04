package com.banking.merchantservice.service;

import com.banking.merchantservice.controller.dto.MerchantDto;
import com.banking.merchantservice.exception.MerchantNotFoundException;
import com.banking.merchantservice.mapper.MerchantMapper;
import com.banking.merchantservice.model.Merchant;
import com.banking.merchantservice.model.MerchantEntity;
import com.banking.merchantservice.repository.MerchantRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {
    private final MerchantRepository merchantRepository;
    private final MerchantEventStore merchantEventStore;
    private final EntityManager entityManager;

    @Transactional
    public Merchant registerMerchant(MerchantDto request) {
        log.info("Registering new merchant: {}", request.name());

        MerchantEntity merchantEntity = MerchantEntity.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .balance(request.initialBalance() != null ? request.initialBalance() : BigDecimal.ZERO)
                .currency(request.currency() != null ? request.currency() : "BRL")
                .build();

        merchantEntity = merchantRepository.save(merchantEntity);

        merchantEventStore.saveMerchantRegisteredEvent(merchantEntity.getId(), merchantEntity.getBalance());

        return MerchantMapper.toDomain(merchantEntity);
    }

    public Merchant getMerchant(UUID merchantId) {
        log.info("Fetching merchant with id: {}", merchantId);
        return merchantRepository.findById(merchantId)
                .map(MerchantMapper::toDomain)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found with id: " + merchantId));
    }

    public Merchant getMerchantByEmail(String email) {
        log.info("Fetching merchant with email: {}", email);
        return merchantRepository.findByEmail(email)
                .map(MerchantMapper::toDomain)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found with email: " + email));
    }

    public List<Merchant> getAllMerchants() {
        log.info("📊 Buscando todos os merchants");
        List<Merchant> merchants = merchantRepository.findAll()
                .stream()
                .map(MerchantMapper::toDomain)
                .toList();
        log.info("✅ {} merchants encontrados", merchants.size());
        return merchants;
    }

    public Merchant getMerchantByPhone(String phone) {
        log.info("Fetching merchant with phone: {}", phone);
        return merchantRepository.findByPhone(phone)
                .map(MerchantMapper::toDomain)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found with phone: " + phone));
    }

    @Transactional
    public void processReceivedPayment(UUID payeeId, java.math.BigDecimal amount) {
        log.info("Processing received payment for merchant: {}, amount: {}", payeeId, amount);

        MerchantEntity merchantEntity = merchantRepository.findById(payeeId)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found with id: " + payeeId));

        Merchant merchant = MerchantMapper.toDomain(merchantEntity);
        merchant.receivePayment(amount);

        MerchantEntity updated = merchantRepository.save(MerchantMapper.toEntity(merchant));

        merchantEventStore.savePaymentReceivedEvent(
                payeeId,
                amount,
                updated.getBalance()
        );

        log.info("Payment processed successfully. New balance: {}", updated.getBalance());
    }

    @Transactional
    public void debitPayer(UUID payerId, BigDecimal amount) {
        log.info("💳 Debitando payer: {}, amount: {}", payerId, amount);

        MerchantEntity merchantEntity = merchantRepository.findById(payerId)
                .orElseThrow(() -> new MerchantNotFoundException("Payer merchant not found: " + payerId));

        Merchant merchant = MerchantMapper.toDomain(merchantEntity);
        merchant.debitPayment(amount);

        MerchantEntity updated = merchantRepository.save(MerchantMapper.toEntity(merchant));

        merchantEventStore.savePaymentDebitedEvent(
                payerId,
                amount.negate(),
                updated.getBalance()
        );

        log.info("✅ Payer debited successfully. New balance: {}", updated.getBalance());
    }
}
