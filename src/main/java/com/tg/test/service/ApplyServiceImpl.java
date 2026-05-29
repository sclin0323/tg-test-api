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

@Service
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private final ApplyRepository applyRepository;

    @Override
    @Transactional
    public Apply create(String applicantName, String title, BigDecimal amount, String remark) {
        return applyRepository.save(Apply.builder()
                .applicantName(applicantName)
                .title(title)
                .amount(amount)
                .remark(remark)
                .build());
    }

    @Override
    @Transactional
    public Apply submit(UUID id) {
        Apply apply = getById(id);
        if (apply.getStatus() != ApplyStatus.DRAFT)
            throw new IllegalStateException("只有草稿可提交，目前：" + apply.getStatus());
        apply.setStatus(ApplyStatus.PENDING);
        return applyRepository.save(apply);
    }

    @Override
    @Transactional
    public Apply approve(UUID id, String reviewerName, String reviewComment) {
        Apply apply = getById(id);
        if (apply.getStatus() != ApplyStatus.PENDING)
            throw new IllegalStateException("只有待審可核准，目前：" + apply.getStatus());
        apply.setStatus(ApplyStatus.APPROVED);
        apply.setReviewerName(reviewerName);
        apply.setReviewComment(reviewComment);
        return applyRepository.save(apply);
    }

    @Override
    @Transactional
    public Apply reject(UUID id, String reviewerName, String reviewComment) {
        Apply apply = getById(id);
        if (apply.getStatus() != ApplyStatus.PENDING)
            throw new IllegalStateException("只有待審可拒絕，目前：" + apply.getStatus());
        apply.setStatus(ApplyStatus.REJECTED);
        apply.setReviewerName(reviewerName);
        apply.setReviewComment(reviewComment);
        return applyRepository.save(apply);
    }

    @Override
    @Transactional(readOnly = true)
    public Apply getById(UUID id) {
        return applyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("申請單不存在：" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apply> getAll() {
        return applyRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apply> getApprovedAndUnpacked() {
        return applyRepository.findApprovedAndUnpacked();
    }
}
