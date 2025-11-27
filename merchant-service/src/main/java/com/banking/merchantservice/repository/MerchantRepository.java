package com.banking.merchantservice.repository;

import com.banking.merchantservice.model.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<MerchantEntity, UUID> {
    Optional<MerchantEntity> findByEmail(String email);
    Optional<MerchantEntity> findByName(String name);
    Optional<MerchantEntity> findByPhone(String phone);
}
