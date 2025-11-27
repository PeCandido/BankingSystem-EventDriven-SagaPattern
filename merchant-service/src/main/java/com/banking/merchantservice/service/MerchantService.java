package com.banking.merchantservice.service;

import com.banking.merchantservice.controller.dto.MerchantDto;
import com.banking.merchantservice.exception.MerchantNotFoundException;
import com.banking.merchantservice.mapper.MerchantMapper;
import com.banking.merchantservice.model.Merchant;
import com.banking.merchantservice.model.MerchantEntity;
import com.banking.merchantservice.repository.MerchantRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {
    private final MerchantRepository merchantRepository;
    private final EntityManager entityManager;

    @Transactional
    public Merchant registerMerchant(MerchantDto request) {
        log.info("Registering new merchant: {}", request.name());

        Merchant merchant = new Merchant(
                request.merchantId(),
                request.name(),
                request.email(),
                request.phone(),
                request.initialBalance(),
                request.currency()
        );

        if (merchant.getId() == null) {
            merchant.initialize(request.initialBalance());
        }

        MerchantEntity merchantEntity = MerchantMapper.toEntity(merchant);

        entityManager.persist(merchantEntity);

        log.info("Merchant registered successfully with id: {}", merchantEntity.getId());
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

    public Merchant getMerchantByPhone(String phone) {
        log.info("Fetching merchant with phone: {}", phone);
        return merchantRepository.findByPhone(phone)
                .map(MerchantMapper::toDomain)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found with phone: " + phone));
    }

    public void processReceivedPayment(UUID payeeId, java.math.BigDecimal amount) {
        log.info("Processing received payment for merchant: {}, amount: {}", payeeId, amount);

        MerchantEntity merchantEntity = merchantRepository.findById(payeeId)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found with id: " + payeeId));

        Merchant merchant = MerchantMapper.toDomain(merchantEntity);
        merchant.receivePayment(amount);

        MerchantEntity updated = merchantRepository.save(MerchantMapper.toEntity(merchant));
        log.info("Payment processed successfully. New balance: {}", updated.getBalance());
    }
}
