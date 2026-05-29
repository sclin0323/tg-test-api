package com.tg.test.data.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 客戶 — 可依性別 / 城市 / 年齡層分析投保分布
 */
@Entity
@Table(name = "DEMO_CUSTOMER")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUSTOMER_ID")
    private Long id;

    @Column(name = "CUSTOMER_NAME", length = 50, nullable = false)
    private String name;

    /** M / F */
    @Column(name = "GENDER", length = 1)
    private String gender;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Column(name = "CITY", length = 30)
    private String city;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Policy> policies = new ArrayList<>();
}
