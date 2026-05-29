package com.tg.test.service;

import com.tg.test.data.entity.Apply;
import com.tg.test.data.entity.Apply.ApplyStatus;
import com.tg.test.data.repository.ApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ApplyService {
    Apply create(String applicantName, String title, BigDecimal amount, String remark);
    Apply submit(UUID id);
    Apply approve(UUID id, String reviewerName, String reviewComment);
    Apply reject(UUID id, String reviewerName, String reviewComment);
    Apply getById(UUID id);
    List<Apply> getAll();
    List<Apply> getApprovedAndUnpacked();
}
