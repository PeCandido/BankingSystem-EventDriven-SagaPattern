package com.banking.payment.repository;

import com.banking.payment.model.PaymentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEventEntity, UUID> {
    List<PaymentEventEntity> findByPaymentId(UUID paymentId);
    List<PaymentEventEntity> findByPayeeId(UUID payeeId);
    List<PaymentEventEntity> findByPayerId(UUID payerId);
}
