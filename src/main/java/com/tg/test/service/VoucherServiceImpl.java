package com.tg.test.service;

import com.tg.test.data.entity.Apply;
import com.tg.test.data.entity.Apply.ApplyStatus;
import com.tg.test.data.entity.Voucher;
import com.tg.test.data.entity.Voucher.VoucherStatus;
import com.tg.test.data.repository.ApplyRepository;
import com.tg.test.data.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final ApplyRepository applyRepository;
    private final AtomicInteger seq = new AtomicInteger(1);

    @Override
    @Transactional
    public Voucher create(String createdByName, String title, String remark, List<UUID> applyIds) {
        if (applyIds == null || applyIds.isEmpty())
            throw new IllegalArgumentException("至少需要一張申請單");

        List<Apply> applies = applyRepository.findAllById(applyIds);
        if (applies.size() != applyIds.size())
            throw new IllegalArgumentException("部分申請單不存在");

        for (Apply a : applies) {
            if (a.getStatus() != ApplyStatus.APPROVED)
                throw new IllegalStateException("申請單 " + a.getId() + " 狀態非核准：" + a.getStatus());
            if (a.getVoucher() != null)
                throw new IllegalStateException("申請單 " + a.getId() + " 已被打包");
        }

        BigDecimal total = applies.stream()
                .map(Apply::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String voucherNo = "VC-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + String.format("%03d", seq.getAndIncrement());

        Voucher voucher = voucherRepository.save(Voucher.builder()
                .voucherNo(voucherNo)
                .title(title)
                .remark(remark)
                .totalAmount(total)
                .createdByName(createdByName)
                .build());

        applies.forEach(a -> {
            a.setStatus(ApplyStatus.PACKED);
            a.setVoucher(voucher);
        });
        applyRepository.saveAll(applies);

        return voucher;
    }

    @Override
    @Transactional
    public Voucher cancel(UUID id) {
        Voucher voucher = getById(id);
        if (voucher.getStatus() != VoucherStatus.PENDING_PAYMENT)
            throw new IllegalStateException("只有待付款可取消，目前：" + voucher.getStatus());

        // 申請單退回核准
        List<Apply> applies = applyRepository.findByVoucherId(id);
        applies.forEach(a -> {
            a.setStatus(ApplyStatus.APPROVED);
            a.setVoucher(null);
        });
        applyRepository.saveAll(applies);

        voucher.setStatus(VoucherStatus.CANCELLED);
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public Voucher getById(UUID id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("傳票不存在：" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Voucher> getAll() {
        return voucherRepository.findAll();
    }
}
