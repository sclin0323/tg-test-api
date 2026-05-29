package com.tg.test.service;

import com.tg.test.data.entity.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    Payment create(UUID voucherId, String payeeName, String payeeAccount,
                   String executedByName, String remark);
    Payment startProcessing(UUID id);
    Payment complete(UUID id);
    Payment fail(UUID id, String reason);
    Payment getById(UUID id);
    List<Payment> getAll();
}
