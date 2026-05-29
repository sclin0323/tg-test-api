package com.tg.test.service;

import com.tg.test.data.entity.Payment;
import com.tg.test.data.entity.Payment.PaymentStatus;
import com.tg.test.data.entity.Voucher;
import com.tg.test.data.entity.Voucher.VoucherStatus;
import com.tg.test.data.repository.PaymentRepository;
import com.tg.test.data.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final VoucherRepository voucherRepository;
    private final AtomicInteger seq = new AtomicInteger(1);

    @Override
    @Transactional
    public Payment create(UUID voucherId, String payeeName, String payeeAccount,
                          String executedByName, String remark) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("傳票不存在：" + voucherId));

        if (voucher.getStatus() != VoucherStatus.PENDING_PAYMENT)
            throw new IllegalStateException("傳票非待付款狀態，目前：" + voucher.getStatus());

        paymentRepository.findByVoucherId(voucherId).ifPresent(p -> {
            throw new IllegalStateException("此傳票已有付款單：" + p.getPaymentNo());
        });

        String paymentNo = "PAY-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + String.format("%03d", seq.getAndIncrement());

        voucher.setStatus(VoucherStatus.PROCESSING);
        voucherRepository.save(voucher);

        return paymentRepository.save(Payment.builder()
                .paymentNo(paymentNo)
                .voucher(voucher)
                .amount(voucher.getTotalAmount())
                .payeeName(payeeName)
                .payeeAccount(payeeAccount)
                .executedByName(executedByName)
                .remark(remark)
                .build());
    }

    @Override
    @Transactional
    public Payment startProcessing(UUID id) {
        Payment payment = getById(id);
        if (payment.getStatus() != PaymentStatus.PENDING)
            throw new IllegalStateException("只有待付款可執行，目前：" + payment.getStatus());
        payment.setStatus(PaymentStatus.PROCESSING);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment complete(UUID id) {
        Payment payment = getById(id);
        if (payment.getStatus() != PaymentStatus.PROCESSING)
            throw new IllegalStateException("只有付款中可完成，目前：" + payment.getStatus());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());

        Voucher voucher = payment.getVoucher();
        voucher.setStatus(VoucherStatus.COMPLETED);
        voucherRepository.save(voucher);

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment fail(UUID id, String reason) {
        Payment payment = getById(id);
        if (payment.getStatus() != PaymentStatus.PROCESSING)
            throw new IllegalStateException("只有付款中可標記失敗，目前：" + payment.getStatus());
        payment.setStatus(PaymentStatus.FAILED);
        payment.setRemark(reason);

        Voucher voucher = payment.getVoucher();
        voucher.setStatus(VoucherStatus.PENDING_PAYMENT);
        voucherRepository.save(voucher);

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("付款單不存在：" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }
}
