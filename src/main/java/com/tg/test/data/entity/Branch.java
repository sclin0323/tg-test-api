package com.tg.test.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 分公司 — 報表常用的最上層分群維度（依區域 / 分公司彙總保費）
 */
@Entity
@Table(name = "DEMO_BRANCH")
@Getter
@Setter
@NoArgsConstructor
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BRANCH_ID")
    private Long id;

    @Column(name = "BRANCH_CODE", length = 20, nullable = false, unique = true)
    private String branchCode;

    @Column(name = "BRANCH_NAME", length = 100, nullable = false)
    private String branchName;

    /** 北區 / 中區 / 南區 / 東區 */
    @Column(name = "REGION", length = 20)
    private String region;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL)
    private List<Agent> agents = new ArrayList<>();
}
