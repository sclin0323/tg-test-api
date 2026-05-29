package com.tg.test.data.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 業務員 — 隸屬一個分公司，承攬多張保單（可做業績排行報表）
 */
@Entity
@Table(name = "DEMO_AGENT")
@Getter
@Setter
@NoArgsConstructor
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AGENT_ID")
    private Long id;

    @Column(name = "AGENT_CODE", length = 20, nullable = false, unique = true)
    private String agentCode;

    @Column(name = "AGENT_NAME", length = 50, nullable = false)
    private String name;

    @Column(name = "HIRE_DATE")
    private LocalDate hireDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID")
    private Branch branch;

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL)
    private List<Policy> policies = new ArrayList<>();
}
