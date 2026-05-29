package com.tg.test.service;

import com.tg.test.data.entity.Voucher;

import java.util.List;
import java.util.UUID;

public interface VoucherService {
    Voucher create(String createdByName, String title, String remark, List<UUID> applyIds);
    Voucher cancel(UUID id);
    Voucher getById(UUID id);
    List<Voucher> getAll();
}
