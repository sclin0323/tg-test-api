package com.tg.test.controller;

import com.tg.test.data.entity.Apply;
import com.tg.test.service.ApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applies")
@RequiredArgsConstructor
@Tag(name = "申請單作業")
public class ApplyController {

    private final ApplyService applyService;

    @Operation(summary = "建立申請單")
    @PostMapping
    public ResponseEntity<Apply> create(@RequestBody CreateRequest req) {
        return ResponseEntity.ok(
                applyService.create(req.applicantName(), req.title(), req.amount(), req.remark()));
    }

    @Operation(summary = "提交申請單（草稿 → 待審）")
    @PatchMapping("/{id}/submit")
    public ResponseEntity<Apply> submit(@PathVariable UUID id) {
        return ResponseEntity.ok(applyService.submit(id));
    }

    @Operation(summary = "核准申請單（待審 → 核准）")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Apply> approve(@PathVariable UUID id, @RequestBody ReviewRequest req) {
        return ResponseEntity.ok(applyService.approve(id, req.reviewerName(), req.reviewComment()));
    }

    @Operation(summary = "拒絕申請單（待審 → 拒絕）")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Apply> reject(@PathVariable UUID id, @RequestBody ReviewRequest req) {
        return ResponseEntity.ok(applyService.reject(id, req.reviewerName(), req.reviewComment()));
    }

    @Operation(summary = "查詢所有申請單")
    @GetMapping
    public ResponseEntity<List<Apply>> getAll() {
        return ResponseEntity.ok(applyService.getAll());
    }

    @Operation(summary = "查詢已核准且可被打包的申請單")
    @GetMapping("/approved-unpacked")
    public ResponseEntity<List<Apply>> getApprovedUnpacked() {
        return ResponseEntity.ok(applyService.getApprovedAndUnpacked());
    }

    @Operation(summary = "查詢單一申請單")
    @GetMapping("/{id}")
    public ResponseEntity<Apply> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(applyService.getById(id));
    }

    public record CreateRequest(String applicantName, String title, BigDecimal amount, String remark) {}
    public record ReviewRequest(String reviewerName, String reviewComment) {}
}
