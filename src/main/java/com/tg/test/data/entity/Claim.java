package com.tg.test.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 理賠 — 隸屬於保單（主從關係），可做理賠率 / 理賠狀態統計，
 * 也很適合 APEX 的 Master-Detail（保單 → 理賠明細）報表。
 */
@Entity
@Table(name = "DEMO_CLAIM")
@Getter
@Setter
@NoArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLAIM_ID")
    private Long id;

    @Column(name = "CLAIM_NO", length = 30, nullable = false, unique = true)
    private String claimNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POLICY_ID", nullable = false)
    private Policy policy;

    @Column(name = "CLAIM_DATE")
    private LocalDate claimDate;

    @Column(name = "CLAIM_AMOUNT", precision = 15, scale = 2)
    private BigDecimal claimAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private ClaimStatus status;

    /** 理賠狀態 */
    public enum ClaimStatus {
        REVIEWING, // 審核中
        APPROVED,  // 已核准
        REJECTED,  // 已拒絕
        PAID       // 已給付
    }
}
