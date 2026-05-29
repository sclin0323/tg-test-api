package com.tg.test.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String paymentNo;       // 付款編號
    private BigDecimal amount;      // 付款金額
    private String payeeName;       // 受款方名稱
    private String payeeAccount;    // 受款方帳號
    private String remark;          // 備注
    private String executedByName;  // 執行付款人姓名

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    // ✅ 加 @JsonIgnore 避免 Payment → Voucher → Payment 無限遞迴
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", unique = true)
    private Voucher voucher;

    private LocalDateTime paidAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PENDING,    // 待付款
        PROCESSING, // 付款中
        COMPLETED,  // 已完成
        FAILED      // 失敗
    }
}