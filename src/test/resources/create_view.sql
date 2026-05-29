-- =========================================================
-- 報表用 VIEW（Oracle 版）
-- 適用：以同一套 JPA Entity DDL 在 Oracle 建立的 DEMO_* 表
--      （未加引號 → 識別字為大寫 DEMO_BRANCH / POLICY_NO ...）
-- 可在 SQL Developer 或 APEX > SQL Workshop > SQL Commands 直接執行。
-- 與 PostgreSQL 版差異極小：函式語法一致，僅識別字大寫。
-- =========================================================

-- ---------------------------------------------------------
-- 報表 1：業務績效總覽（彙總）
-- ---------------------------------------------------------
CREATE OR REPLACE VIEW V_AGENT_PERFORMANCE AS
SELECT
    b.region                                            AS region,
    b.branch_name                                       AS branch_name,
    ag.agent_name                                       AS agent_name,
    COUNT(p.policy_id)                                  AS policy_count,
    COALESCE(SUM(p.premium_amount), 0)                  AS total_premium,
    COUNT(CASE WHEN p.status = 'ACTIVE' THEN 1 END)     AS active_count,
    ROUND(100.0 * COUNT(CASE WHEN p.status = 'ACTIVE' THEN 1 END)
              / NULLIF(COUNT(p.policy_id), 0), 1)     AS active_rate_pct
FROM DEMO_AGENT ag
         JOIN DEMO_BRANCH b      ON b.branch_id = ag.branch_id
         LEFT JOIN DEMO_POLICY p ON p.agent_id  = ag.agent_id
GROUP BY b.region, b.branch_name, ag.agent_name
ORDER BY total_premium DESC;


-- ---------------------------------------------------------
-- 報表 2：商品別理賠分析（只計 已核准/已給付 的賠款）
-- ---------------------------------------------------------
CREATE OR REPLACE VIEW V_PRODUCT_CLAIM_ANALYSIS AS
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
FROM DEMO_POLICY p
         LEFT JOIN (
    SELECT policy_id, SUM(claim_amount) AS paid_amount
    FROM DEMO_CLAIM
    WHERE status IN ('PAID', 'APPROVED')
    GROUP BY policy_id
) c ON c.policy_id = p.policy_id
GROUP BY p.product_type, c.paid_amount
ORDER BY loss_ratio_pct DESC NULLS LAST;


-- ---------------------------------------------------------
-- 報表 3：保單明細清單（明細層，可多筆、分頁）
-- ---------------------------------------------------------
CREATE OR REPLACE VIEW V_POLICY_DETAIL AS
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
FROM DEMO_POLICY p
         JOIN      DEMO_CUSTOMER cu ON cu.customer_id = p.customer_id
         LEFT JOIN DEMO_AGENT    ag ON ag.agent_id    = p.agent_id
         LEFT JOIN DEMO_BRANCH   b  ON b.branch_id    = ag.branch_id
         LEFT JOIN (
    SELECT policy_id, COUNT(*) AS claim_count
    FROM DEMO_CLAIM
    GROUP BY policy_id
) cl ON cl.policy_id = p.policy_id
ORDER BY p.policy_no;