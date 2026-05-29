package com.tg.test.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 建立報表用的 VIEW（PostgreSQL）。目前共三支：
 *   1. v_agent_performance      業務績效總覽（彙總）
 *   2. v_product_claim_analysis 商品別理賠分析（彙總）
 *   3. v_policy_detail          保單明細清單（明細，可多筆、分頁）
 *
 *  - @ActiveProfiles("test")：載入 application-test.yml 的資料源。
 *  - @Transactional + @Commit：PG 的 DDL 受交易控制，加 @Commit 才會真的建立。
 *  - 先 DROP VIEW IF EXISTS 再 CREATE，可重複執行。
 *
 * 執行：IDE 右鍵 Run，或  mvn test -Dtest=DemoViewSetupTest
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Commit
class DemoViewSetupTest {

    @PersistenceContext
    private EntityManager em;

    /** 報表 1：業務績效總覽 */
    private static final String V_AGENT_PERFORMANCE = """
            CREATE VIEW v_agent_performance AS
            SELECT
                b.region                                            AS region,
                b.branch_name                                       AS branch_name,
                ag.agent_name                                       AS agent_name,
                COUNT(p.policy_id)                                  AS policy_count,
                COALESCE(SUM(p.premium_amount), 0)                  AS total_premium,
                COUNT(CASE WHEN p.status = 'ACTIVE' THEN 1 END)     AS active_count,
                ROUND(100.0 * COUNT(CASE WHEN p.status = 'ACTIVE' THEN 1 END)
                            / NULLIF(COUNT(p.policy_id), 0), 1)     AS active_rate_pct
            FROM demo_agent ag
            JOIN demo_branch b      ON b.branch_id = ag.branch_id
            LEFT JOIN demo_policy p ON p.agent_id  = ag.agent_id
            GROUP BY b.region, b.branch_name, ag.agent_name
            ORDER BY total_premium DESC
            """;

    /** 報表 2：商品別理賠分析（只計 已核准/已給付 的賠款） */
    private static final String V_PRODUCT_CLAIM_ANALYSIS = """
            CREATE VIEW v_product_claim_analysis AS
            SELECT
                p.product_type                                      AS product_type,
                CASE p.product_type
                    WHEN 'LIFE'     THEN '壽險'
                    WHEN 'MEDICAL'  THEN '醫療險'
                    WHEN 'ACCIDENT' THEN '意外險'
                    WHEN 'AUTO'     THEN '車險'
                    WHEN 'TRAVEL'   THEN '旅平險'
                END                                                 AS product_name,
                COUNT(p.policy_id)                                  AS policy_count,
                COALESCE(SUM(p.premium_amount), 0)                  AS total_premium,
                COALESCE(c.paid_amount, 0)                          AS total_claim_amount,
                ROUND(100.0 * COALESCE(c.paid_amount, 0)
                            / NULLIF(SUM(p.premium_amount), 0), 1)  AS loss_ratio_pct
            FROM demo_policy p
            LEFT JOIN (
                SELECT policy_id, SUM(claim_amount) AS paid_amount
                FROM demo_claim
                WHERE status IN ('PAID', 'APPROVED')
                GROUP BY policy_id
            ) c ON c.policy_id = p.policy_id
            GROUP BY p.product_type, c.paid_amount
            ORDER BY loss_ratio_pct DESC NULLS LAST
            """;

    /** 報表 3：保單明細清單（明細層，可多筆、分頁） */
    private static final String V_POLICY_DETAIL = """
            CREATE VIEW v_policy_detail AS
            SELECT
                p.policy_no                                 AS policy_no,
                cu.customer_name                            AS customer_name,
                ag.agent_name                               AS agent_name,
                b.branch_name                               AS branch_name,
                b.region                                    AS region,
                CASE p.product_type
                    WHEN 'LIFE'     THEN '壽險'
                    WHEN 'MEDICAL'  THEN '醫療險'
                    WHEN 'ACCIDENT' THEN '意外險'
                    WHEN 'AUTO'     THEN '車險'
                    WHEN 'TRAVEL'   THEN '旅平險'
                END                                         AS product_name,
                p.premium_amount                            AS premium_amount,
                p.coverage_amount                           AS coverage_amount,
                p.start_date                                AS start_date,
                p.end_date                                  AS end_date,
                CASE p.status
                    WHEN 'ACTIVE'      THEN '有效'
                    WHEN 'LAPSED'      THEN '失效'
                    WHEN 'EXPIRED'     THEN '已到期'
                    WHEN 'SURRENDERED' THEN '已解約'
                END                                         AS status_name,
                COALESCE(cl.claim_count, 0)                 AS claim_count
            FROM demo_policy p
            JOIN      demo_customer cu ON cu.customer_id = p.customer_id
            LEFT JOIN demo_agent    ag ON ag.agent_id    = p.agent_id
            LEFT JOIN demo_branch   b  ON b.branch_id    = ag.branch_id
            LEFT JOIN (
                SELECT policy_id, COUNT(*) AS claim_count
                FROM demo_claim
                GROUP BY policy_id
            ) cl ON cl.policy_id = p.policy_id
            ORDER BY p.policy_no
            """;

    @Test
    void createReportViews() {

        // 先刪再建，確保可重複執行
        em.createNativeQuery("DROP VIEW IF EXISTS v_agent_performance").executeUpdate();
        em.createNativeQuery("DROP VIEW IF EXISTS v_product_claim_analysis").executeUpdate();
        em.createNativeQuery("DROP VIEW IF EXISTS v_policy_detail").executeUpdate();

        em.createNativeQuery(V_AGENT_PERFORMANCE).executeUpdate();
        em.createNativeQuery(V_PRODUCT_CLAIM_ANALYSIS).executeUpdate();
        em.createNativeQuery(V_POLICY_DETAIL).executeUpdate();

        // 建立後做個 smoke test：能查、不報錯
        Object n1 = em.createNativeQuery("SELECT COUNT(*) FROM v_agent_performance").getSingleResult();
        Object n2 = em.createNativeQuery("SELECT COUNT(*) FROM v_product_claim_analysis").getSingleResult();
        Object n3 = em.createNativeQuery("SELECT COUNT(*) FROM v_policy_detail").getSingleResult();

        System.out.printf("View 建立完成 → v_agent_performance:%s 列, v_product_claim_analysis:%s 列, v_policy_detail:%s 列%n",
                n1, n2, n3);
    }
}