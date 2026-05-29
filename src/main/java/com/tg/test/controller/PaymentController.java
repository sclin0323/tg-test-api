package com.tg.test.controller;

import com.tg.test.data.entity.Payment;
import com.tg.test.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "付款作業")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "建立付款單")
    @PostMapping
    public ResponseEntity<Payment> create(@RequestBody CreateRequest req) {
        return ResponseEntity.ok(
                paymentService.create(req.voucherId(), req.payeeName(),
                        req.payeeAccount(), req.executedByName(), req.remark()));
    }

    @Operation(summary = "開始付款（待付款 → 付款中）")
    @PatchMapping("/{id}/start")
    public ResponseEntity<Payment> start(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.startProcessing(id));
    }

    @Operation(summary = "完成付款（付款中 → 已完成）")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Payment> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.complete(id));
    }

    @Operation(summary = "付款失敗（付款中 → 失敗）")
    @PatchMapping("/{id}/fail")
    public ResponseEntity<Payment> fail(@PathVariable UUID id, @RequestBody FailRequest req) {
        return ResponseEntity.ok(paymentService.fail(id, req.reason()));
    }

    @Operation(summary = "查詢所有付款單")
    @GetMapping
    public ResponseEntity<List<Payment>> getAll() {
        return ResponseEntity.ok(paymentService.getAll());
    }

    @Operation(summary = "查詢單一付款單")
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    public record CreateRequest(UUID voucherId, String payeeName, String payeeAccount,
                                String executedByName, String remark) {}
    public record FailRequest(String reason) {}
}
