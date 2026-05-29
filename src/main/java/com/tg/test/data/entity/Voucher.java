package com.tg.test.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "voucher")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String voucherNo;       // 傳票編號
    private String title;           // 傳票標題
    private BigDecimal totalAmount; // 總金額
    private String remark;          // 備注
    private String createdByName;   // 建立人姓名

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VoucherStatus status = VoucherStatus.PENDING_PAYMENT;

    // ✅ 加 @JsonIgnore 避免 Voucher → Apply → Voucher 無限遞迴
    @JsonIgnore
    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Apply> applies = new ArrayList<>();

    // ✅ 加 @JsonIgnore 避免 Voucher → Payment → Voucher 無限遞迴
    @JsonIgnore
    @OneToOne(mappedBy = "voucher", fetch = FetchType.LAZY)
    private Payment payment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum VoucherStatus {
        PENDING_PAYMENT, // 待付款
        PROCESSING,      // 付款中
        COMPLETED,       // 已完成
        CANCELLED        // 已取消
    }
}