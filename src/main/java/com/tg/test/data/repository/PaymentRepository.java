package com.tg.test.data.repository;

import com.tg.test.data.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByVoucherId(UUID voucherId);

    List<Payment> findByStatus(Payment.PaymentStatus status);
}
