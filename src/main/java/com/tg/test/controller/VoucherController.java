package com.tg.test.controller;

import com.tg.test.data.entity.Voucher;
import com.tg.test.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Tag(name = "傳票管理作業")
public class VoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "建立傳票（打包多張申請單）")
    @PostMapping
    public ResponseEntity<Voucher> create(@RequestBody CreateRequest req) {
        return ResponseEntity.ok(
                voucherService.create(req.createdByName(), req.title(), req.remark(), req.applyIds()));
    }

    @Operation(summary = "取消傳票")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Voucher> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(voucherService.cancel(id));
    }

    @Operation(summary = "查詢所有傳票")
    @GetMapping
    public ResponseEntity<List<Voucher>> getAll() {
        return ResponseEntity.ok(voucherService.getAll());
    }

    @Operation(summary = "查詢單一傳票")
    @GetMapping("/{id}")
    public ResponseEntity<Voucher> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(voucherService.getById(id));
    }

    public record CreateRequest(String createdByName, String title, String remark, List<UUID> applyIds) {}
}
