package com.banking.merchantservice.repository;

import com.banking.merchantservice.model.MerchantEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MerchantEventRepository extends JpaRepository<MerchantEventEntity, UUID> {
    List<MerchantEventEntity> findByMerchantId(UUID merchantId);
    List<MerchantEventEntity> findByEventType(String eventType);
}
