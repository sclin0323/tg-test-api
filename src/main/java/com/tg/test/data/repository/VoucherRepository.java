package com.tg.test.data.repository;

import com.tg.test.data.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, UUID> {

    List<Voucher> findByStatus(Voucher.VoucherStatus status);
}
