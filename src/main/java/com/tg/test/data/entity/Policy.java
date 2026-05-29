package com.tg.test.data.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 保單 — 報表的核心事實表（fact table）。
 * 含金額（保費 / 保額）、日期（可做時間序列）、商品別與狀態（可做篩選與分群）。
 */
@Entity
@Table(name = "DEMO_POLICY")
@Getter
@Setter
@NoArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POLICY_ID")
    private Long id;

    @Column(name = "POLICY_NO", length = 30, nullable = false, unique = true)
    private String policyNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AGENT_ID")
    private Agent agent;

    @Enumerated(EnumType.STRING)
    @Column(name = "PRODUCT_TYPE", length = 20, nullable = false)
    private ProductType productType;

    /** 年繳保費 */
    @Column(name = "PREMIUM_AMOUNT", precision = 15, scale = 2)
    private BigDecimal premiumAmount;

    /** 保額 */
    @Column(name = "COVERAGE_AMOUNT", precision = 15, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private PolicyStatus status;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL)
    private List<Claim> claims = new ArrayList<>();

    /** 商品別 */
    public enum ProductType {
        LIFE,      // 壽險
        MEDICAL,   // 醫療險
        ACCIDENT,  // 意外險
        AUTO,      // 車險
        TRAVEL     // 旅平險
    }

    /** 保單狀態 */
    public enum PolicyStatus {
        ACTIVE,      // 有效
        LAPSED,      // 失效
        EXPIRED,     // 已到期
        SURRENDERED  // 已解約
    }
}
