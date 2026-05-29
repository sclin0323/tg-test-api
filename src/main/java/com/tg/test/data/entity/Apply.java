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
@Table(name = "apply")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Apply {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String applicantName;   // 申請人姓名
    private String title;           // 申請標題
    private BigDecimal amount;      // 申請金額
    private String remark;          // 備注

    private String reviewerName;    // 審核人姓名
    private String reviewComment;   // 審核意見

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplyStatus status = ApplyStatus.DRAFT;

    // ✅ 加 @JsonIgnore 避免 Apply → Voucher → Apply 無限遞迴
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum ApplyStatus {
        DRAFT,    // 草稿
        PENDING,  // 待審核
        APPROVED, // 已核准
        REJECTED, // 已拒絕
        PACKED    // 已打包
    }
}