package com.tg.test.data;

import com.tg.test.data.entity.Agent;
import com.tg.test.data.entity.Branch;
import com.tg.test.data.entity.Claim;
import com.tg.test.data.entity.Customer;
import com.tg.test.data.entity.Policy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 寫入「具規模」的保險示範資料（給報表 DEMO 用）。
 *
 * 重點：
 *  - 每次執行「先清空」舊資料（子表 → 父表），可重複跑。
 *  - 用迴圈 + 固定亂數種子 Random(42) 產生資料：每次結果一致、報表數字可重現。
 *  - 規模在下方常數調整：客戶 / 保單 / 業務員數量。
 *
 * 注意：資料是逐筆 INSERT（IDENTITY 主鍵無法批次），若 DB 在遠端，
 *      筆數很大時會比較慢。先用預設值試跑，再視需要往上加。
 *
 * 執行：IDE 右鍵 Run，或  mvn test -Dtest=DemoDataSeederTest
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Commit
class DemoDataSeederTest {

    // ====== 規模設定（要更大就改這裡）======
    private static final int AGENT_COUNT = 40;
    private static final int CUSTOMER_COUNT = 600;
    private static final int POLICY_COUNT = 2000;
    private static final double CLAIM_PROBABILITY = 0.22;  // 約 22% 保單會有理賠
    private static final int FLUSH_EVERY = 200;            // 每 N 筆 flush 一次

    @PersistenceContext
    private EntityManager em;

    private final Random rnd = new Random(42);
    private final LocalDate today = LocalDate.now();

    // ====== 產生資料用的素材 ======
    private static final String[][] BRANCH_DEFS = {
            {"B01", "台北分公司", "北區"},
            {"B02", "新北分公司", "北區"},
            {"B03", "桃園分公司", "北區"},
            {"B04", "台中分公司", "中區"},
            {"B05", "台南分公司", "南區"},
            {"B06", "高雄分公司", "南區"},
    };
    private static final String[] SURNAMES = {
            "王", "李", "張", "劉", "陳", "楊", "黃", "趙", "吳", "周",
            "徐", "孫", "馬", "朱", "胡", "林", "郭", "何", "高", "羅",
            "鄭", "梁", "謝", "宋", "唐", "許", "鄧", "馮", "韓", "曹"
    };
    private static final String[] GIVEN_NAMES = {
            "明", "華", "強", "偉", "芳", "敏", "靜", "麗", "軍", "洋",
            "勇", "傑", "娟", "濤", "超", "霞", "平", "剛", "建國", "志成",
            "家豪", "淑芬", "俊傑", "宜君", "雅婷", "美玲", "文彬", "佩珊", "怡君", "冠廷"
    };
    private static final String[] CITIES = {
            "台北市", "新北市", "桃園市", "台中市", "台南市", "高雄市", "基隆市", "新竹市", "嘉義市"
    };
    // 加權後的商品別（LIFE / MEDICAL 較常見）
    private static final Policy.ProductType[] PRODUCTS = {
            Policy.ProductType.LIFE, Policy.ProductType.LIFE,
            Policy.ProductType.MEDICAL, Policy.ProductType.MEDICAL, Policy.ProductType.MEDICAL,
            Policy.ProductType.ACCIDENT,
            Policy.ProductType.AUTO, Policy.ProductType.AUTO,
            Policy.ProductType.TRAVEL
    };
    // 加權後的理賠狀態
    private static final Claim.ClaimStatus[] CLAIM_STATUSES = {
            Claim.ClaimStatus.PAID, Claim.ClaimStatus.PAID, Claim.ClaimStatus.PAID, Claim.ClaimStatus.PAID,
            Claim.ClaimStatus.APPROVED, Claim.ClaimStatus.APPROVED, Claim.ClaimStatus.APPROVED,
            Claim.ClaimStatus.REVIEWING, Claim.ClaimStatus.REVIEWING,
            Claim.ClaimStatus.REJECTED
    };

    @Test
    void seedDemoData() {

        // 1) 先清空舊資料（順序：子表 → 父表，避免外鍵衝突）
        em.createQuery("DELETE FROM Claim").executeUpdate();
        em.createQuery("DELETE FROM Policy").executeUpdate();
        em.createQuery("DELETE FROM Customer").executeUpdate();
        em.createQuery("DELETE FROM Agent").executeUpdate();
        em.createQuery("DELETE FROM Branch").executeUpdate();
        em.flush();

        // 2) 分公司
        List<Branch> branches = new ArrayList<>();
        for (String[] def : BRANCH_DEFS) {
            branches.add(branch(def[0], def[1], def[2]));
        }

        // 3) 業務員
        List<Agent> agents = new ArrayList<>();
        for (int i = 1; i <= AGENT_COUNT; i++) {
            Branch b = branches.get(rnd.nextInt(branches.size()));
            LocalDate hire = randomDate(today.minusYears(8), today.minusMonths(1));
            agents.add(agent(String.format("A%04d", i), randomName(), hire, b));
        }
        em.flush();

        // 4) 客戶
        List<Customer> customers = new ArrayList<>();
        for (int i = 1; i <= CUSTOMER_COUNT; i++) {
            LocalDate birth = today.minusYears(randInt(20, 70)).minusDays(randInt(0, 364));
            customers.add(customer(randomName(), rnd.nextBoolean() ? "M" : "F", birth, pick(CITIES)));
            if (i % FLUSH_EVERY == 0) em.flush();
        }
        em.flush();

        // 5) 保單（核心事實表）
        List<Policy> policies = new ArrayList<>();
        for (int i = 1; i <= POLICY_COUNT; i++) {
            Customer c = customers.get(rnd.nextInt(customers.size()));
            Agent a = agents.get(rnd.nextInt(agents.size()));
            Policy.ProductType pt = pick(PRODUCTS);

            long premium = premiumFor(pt);
            long coverage = coverageFor(pt, premium);
            LocalDate start = randomDate(today.minusDays(1095), today);     // 近 3 年內承保
            LocalDate end = endDateFor(pt, start);
            Policy.PolicyStatus st = statusFor(end);

            policies.add(policy(String.format("P%07d", i), c, a, pt, premium, coverage, start, end, st));
            if (i % FLUSH_EVERY == 0) em.flush();
        }
        em.flush();

        // 6) 理賠（約 CLAIM_PROBABILITY 的保單會有 1~2 筆）
        int claimCount = 0;
        int seq = 1;
        for (Policy p : policies) {
            if (rnd.nextDouble() >= CLAIM_PROBABILITY) continue;
            int n = rnd.nextDouble() < 0.2 ? 2 : 1;
            for (int k = 0; k < n; k++) {
                LocalDate upper = p.getEndDate().isBefore(today) ? p.getEndDate() : today;
                if (upper.isBefore(p.getStartDate())) upper = p.getStartDate();
                LocalDate cd = randomDate(p.getStartDate(), upper);
                claim(String.format("CL%07d", seq++), p, cd, claimAmount(p), pick(CLAIM_STATUSES));
                claimCount++;
                if (claimCount % FLUSH_EVERY == 0) em.flush();
            }
        }
        em.flush();

        // 7) 驗證 + 摘要
        long branchN = count("Branch");
        long agentN = count("Agent");
        long customerN = count("Customer");
        long policyN = count("Policy");
        long claimN = count("Claim");

        System.out.printf("已寫入 Demo 資料 → 分公司:%d 業務員:%d 客戶:%d 保單:%d 理賠:%d%n",
                branchN, agentN, customerN, policyN, claimN);

        assertEquals(BRANCH_DEFS.length, branchN);
        assertEquals(AGENT_COUNT, agentN);
        assertEquals(CUSTOMER_COUNT, customerN);
        assertEquals(POLICY_COUNT, policyN);
        assertEquals(claimCount, claimN);
    }

    // ========== 業務邏輯小工具 ==========

    private long premiumFor(Policy.ProductType pt) {
        int v = switch (pt) {
            case LIFE -> randInt(24000, 120000);
            case MEDICAL -> randInt(8000, 30000);
            case ACCIDENT -> randInt(3000, 12000);
            case AUTO -> randInt(12000, 40000);
            case TRAVEL -> randInt(500, 3000);
        };
        return (v / 100) * 100L;   // 取整到百元
    }

    private long coverageFor(Policy.ProductType pt, long premium) {
        int factor = switch (pt) {
            case LIFE -> randInt(50, 120);
            case MEDICAL -> randInt(60, 100);
            case ACCIDENT -> randInt(200, 500);
            case AUTO -> randInt(30, 60);
            case TRAVEL -> randInt(200, 600);
        };
        return premium * factor;
    }

    private LocalDate endDateFor(Policy.ProductType pt, LocalDate start) {
        return switch (pt) {
            case LIFE -> start.plusYears(20);
            case TRAVEL -> start.plusDays(randInt(7, 30));
            default -> start.plusYears(1);   // MEDICAL / ACCIDENT / AUTO
        };
    }

    private Policy.PolicyStatus statusFor(LocalDate end) {
        double r = rnd.nextDouble();
        if (end.isBefore(today)) {
            if (r < 0.10) return Policy.PolicyStatus.LAPSED;
            if (r < 0.15) return Policy.PolicyStatus.SURRENDERED;
            return Policy.PolicyStatus.EXPIRED;
        } else {
            if (r < 0.08) return Policy.PolicyStatus.LAPSED;
            if (r < 0.12) return Policy.PolicyStatus.SURRENDERED;
            return Policy.PolicyStatus.ACTIVE;
        }
    }

    private long claimAmount(Policy p) {
        long raw = randInt(10000, 800000);
        long cap = p.getCoverageAmount().longValue();
        long amt = Math.min(raw, cap);
        return (amt / 100) * 100L;
    }

    // ========== 通用小工具 ==========

    private int randInt(int minInclusive, int maxInclusive) {
        return minInclusive + rnd.nextInt(maxInclusive - minInclusive + 1);
    }

    private <T> T pick(T[] arr) {
        return arr[rnd.nextInt(arr.length)];
    }

    private LocalDate randomDate(LocalDate from, LocalDate to) {
        long days = ChronoUnit.DAYS.between(from, to);
        if (days <= 0) return from;
        return from.plusDays((long) (rnd.nextDouble() * days));
    }

    private String randomName() {
        return pick(SURNAMES) + pick(GIVEN_NAMES);
    }

    // ========== persist helper ==========

    private Branch branch(String code, String name, String region) {
        Branch b = new Branch();
        b.setBranchCode(code);
        b.setBranchName(name);
        b.setRegion(region);
        em.persist(b);
        return b;
    }

    private Agent agent(String code, String name, LocalDate hireDate, Branch branch) {
        Agent a = new Agent();
        a.setAgentCode(code);
        a.setName(name);
        a.setHireDate(hireDate);
        a.setBranch(branch);
        em.persist(a);
        return a;
    }

    private Customer customer(String name, String gender, LocalDate birth, String city) {
        Customer c = new Customer();
        c.setName(name);
        c.setGender(gender);
        c.setBirthDate(birth);
        c.setCity(city);
        em.persist(c);
        return c;
    }

    private Policy policy(String policyNo, Customer customer, Agent agent,
                          Policy.ProductType productType, long premium, long coverage,
                          LocalDate start, LocalDate end, Policy.PolicyStatus status) {
        Policy p = new Policy();
        p.setPolicyNo(policyNo);
        p.setCustomer(customer);
        p.setAgent(agent);
        p.setProductType(productType);
        p.setPremiumAmount(BigDecimal.valueOf(premium));
        p.setCoverageAmount(BigDecimal.valueOf(coverage));
        p.setStartDate(start);
        p.setEndDate(end);
        p.setStatus(status);
        em.persist(p);
        return p;
    }

    private Claim claim(String claimNo, Policy policy, LocalDate date,
                        long amount, Claim.ClaimStatus status) {
        Claim cl = new Claim();
        cl.setClaimNo(claimNo);
        cl.setPolicy(policy);
        cl.setClaimDate(date);
        cl.setClaimAmount(BigDecimal.valueOf(amount));
        cl.setStatus(status);
        em.persist(cl);
        return cl;
    }

    private long count(String entityName) {
        return em.createQuery("SELECT COUNT(e) FROM " + entityName + " e", Long.class)
                .getSingleResult();
    }
}