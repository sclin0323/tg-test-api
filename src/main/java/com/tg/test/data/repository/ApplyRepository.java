package com.tg.test.data.repository;

import com.tg.test.data.entity.Apply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplyRepository extends JpaRepository<Apply, UUID> {

    List<Apply> findByStatus(Apply.ApplyStatus status);

    List<Apply> findByVoucherId(UUID voucherId);

    @Query("SELECT a FROM Apply a WHERE a.status = 'APPROVED' AND a.voucher IS NULL")
    List<Apply> findApprovedAndUnpacked();
}
