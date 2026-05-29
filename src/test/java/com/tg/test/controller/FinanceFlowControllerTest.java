package com.tg.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tg.test.data.repository.ApplyRepository;
import com.tg.test.data.repository.PaymentRepository;
import com.tg.test.data.repository.VoucherRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 財務系統完整流程 - MockMvc 整合測試
 * 直接打 dev PostgreSQL DB
 *
 * 情境：
 *   張小明（差旅費 15,000）＋ 李小花（教育訓練費 8,000）
 *   → 財務承辦人 陳美玲 審核 → 打包傳票 → 完成付款
 *
 * 清理機制：
 *   @AfterAll 自動刪除本次測試產生的資料，保持 DB 乾淨
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")  // 直接使用開發 DB
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 讓 @AfterAll 可以存取 instance 變數
class FinanceFlowControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // 注入 Repository 供 @AfterAll 清理使用
    @Autowired ApplyRepository applyRepository;
    @Autowired VoucherRepository voucherRepository;
    @Autowired PaymentRepository paymentRepository;

    // 跨 Step 共享的 ID（本次測試產生）
    String applyId1;
    String applyId2;
    String voucherId;
    String paymentId;

    // ═══════════════════════════════════════════════════
    // 測試結束後清理本次資料
    // ═══════════════════════════════════════════════════

    @AfterAll
    void 清理本次測試資料() {
        System.out.println("🧹 開始清理本次測試資料...");

        // 注意刪除順序：payment → voucher → apply（FK 限制）
        if (paymentId != null) {
            paymentRepository.deleteById(UUID.fromString(paymentId));
            System.out.println("✅ 已刪除付款單：" + paymentId);
        }
        if (voucherId != null) {
            // 先解除申請單的 voucher FK
            applyRepository.findByVoucherId(UUID.fromString(voucherId))
                    .forEach(a -> {
                        a.setVoucher(null);
                        applyRepository.save(a);
                    });
            voucherRepository.deleteById(UUID.fromString(voucherId));
            System.out.println("✅ 已刪除傳票：" + voucherId);
        }
        if (applyId1 != null) {
            applyRepository.deleteById(UUID.fromString(applyId1));
            System.out.println("✅ 已刪除申請單1（張小明）：" + applyId1);
        }
        if (applyId2 != null) {
            applyRepository.deleteById(UUID.fromString(applyId2));
            System.out.println("✅ 已刪除申請單2（李小花）：" + applyId2);
        }

        System.out.println("🎉 清理完成，DB 保持乾淨");
    }

    // ═══════════════════════════════════════════════════
    // 申請階段
    // ═══════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("Step 1｜張小明 建立申請單（草稿）")
    void step1_張小明建立申請單() throws Exception {

        String body = objectMapper.writeValueAsString(Map.of(
                "applicantName", "張小明",
                "title", "2024Q2 差旅費",
                "amount", 15000,
                "remark", "台北出差三天"
        ));

        MvcResult result = mockMvc.perform(post("/api/applies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.applicantName").value("張小明"))
                .andExpect(jsonPath("$.amount").value(15000))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        applyId1 = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asText();

        System.out.println("📝 申請單1 建立：" + applyId1);
    }

    @Test
    @Order(2)
    @DisplayName("Step 2｜張小明 提交申請單（DRAFT → PENDING）")
    void step2_張小明提交申請單() throws Exception {

        mockMvc.perform(patch("/api/applies/{id}/submit", applyId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @Order(3)
    @DisplayName("Step 3｜李小花 建立申請單（草稿）")
    void step3_李小花建立申請單() throws Exception {

        String body = objectMapper.writeValueAsString(Map.of(
                "applicantName", "李小花",
                "title", "2024Q2 教育訓練費",
                "amount", 8000,
                "remark", "外部課程報名費"
        ));

        MvcResult result = mockMvc.perform(post("/api/applies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicantName").value("李小花"))
                .andExpect(jsonPath("$.amount").value(8000))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        applyId2 = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asText();

        System.out.println("📝 申請單2 建立：" + applyId2);
    }

    @Test
    @Order(4)
    @DisplayName("Step 4｜李小花 提交申請單（DRAFT → PENDING）")
    void step4_李小花提交申請單() throws Exception {

        mockMvc.perform(patch("/api/applies/{id}/submit", applyId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // ═══════════════════════════════════════════════════
    // 審核階段
    // ═══════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("Step 5｜陳美玲 核准 張小明的申請單（PENDING → APPROVED）")
    void step5_陳美玲核准張小明申請單() throws Exception {

        String body = objectMapper.writeValueAsString(Map.of(
                "reviewerName", "陳美玲",
                "reviewComment", "單據齊全，核准"
        ));

        mockMvc.perform(patch("/api/applies/{id}/approve", applyId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewerName").value("陳美玲"));
    }

    @Test
    @Order(6)
    @DisplayName("Step 6｜陳美玲 核准 李小花的申請單（PENDING → APPROVED）")
    void step6_陳美玲核准李小花申請單() throws Exception {

        String body = objectMapper.writeValueAsString(Map.of(
                "reviewerName", "陳美玲",
                "reviewComment", "核准"
        ));

        mockMvc.perform(patch("/api/applies/{id}/approve", applyId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewerName").value("陳美玲"));
    }

    // ═══════════════════════════════════════════════════
    // 傳票打包階段
    // ═══════════════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("Step 7｜查詢可打包申請單（本次應包含新建的2筆）")
    void step7_查詢可打包申請單() throws Exception {

        MvcResult result = mockMvc.perform(get("/api/applies/approved-unpacked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("APPROVED"))))
                .andReturn();

        // DB 可能有其他舊資料，只確認本次的兩張都在清單內
        String content = result.getResponse().getContentAsString();
        Assertions.assertTrue(content.contains(applyId1), "申請單1 應在可打包清單中");
        Assertions.assertTrue(content.contains(applyId2), "申請單2 應在可打包清單中");
    }

    @Test
    @Order(8)
    @DisplayName("Step 8｜陳美玲 建立傳票，打包兩張申請單")
    void step8_建立傳票打包兩張申請單() throws Exception {

        String body = objectMapper.writeValueAsString(Map.of(
                "createdByName", "陳美玲",
                "title", "2024Q2 五月份費用傳票",
                "remark", "差旅費 + 教育訓練費",
                "applyIds", List.of(applyId1, applyId2)
        ));

        MvcResult result = mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voucherNo").value(startsWith("VC-")))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalAmount").value(23000))
                .andExpect(jsonPath("$.createdByName").value("陳美玲"))
                .andReturn();

        voucherId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asText();

        System.out.println("🗂️  傳票 建立：" + voucherId);
    }

    @Test
    @Order(9)
    @DisplayName("Step 8-確認｜打包後兩張申請單狀態應為 PACKED")
    void step9_確認申請單狀態為PACKED() throws Exception {

        mockMvc.perform(get("/api/applies/{id}", applyId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PACKED"));

        mockMvc.perform(get("/api/applies/{id}", applyId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PACKED"));
    }

    // ═══════════════════════════════════════════════════
    // 付款階段
    // ═══════════════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("Step 9｜陳美玲 建立付款單")
    void step10_建立付款單() throws Exception {

        String body = objectMapper.writeValueAsString(Map.of(
                "voucherId", voucherId,
                "payeeName", "張小明／李小花",
                "payeeAccount", "012-345678",
                "executedByName", "陳美玲",
                "remark", "五月份費用統一匯款"
        ));

        MvcResult result = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentNo").value(startsWith("PAY-")))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(23000))
                .andReturn();

        paymentId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asText();

        System.out.println("💳 付款單 建立：" + paymentId);
    }

    @Test
    @Order(11)
    @DisplayName("Step 9-確認｜建立付款單後傳票狀態應為 PROCESSING")
    void step11_傳票狀態應為PROCESSING() throws Exception {

        mockMvc.perform(get("/api/vouchers/{id}", voucherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @Order(12)
    @DisplayName("Step 10｜開始執行付款（PENDING → PROCESSING）")
    void step12_開始執行付款() throws Exception {

        mockMvc.perform(patch("/api/payments/{id}/start", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @Order(13)
    @DisplayName("Step 11｜確認付款完成（PROCESSING → COMPLETED）")
    void step13_確認付款完成() throws Exception {

        mockMvc.perform(patch("/api/payments/{id}/complete", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.paidAt").isNotEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("Step 11-確認｜付款完成後傳票狀態應同步為 COMPLETED")
    void step14_傳票狀態同步為COMPLETED() throws Exception {

        mockMvc.perform(get("/api/vouchers/{id}", voucherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    // ═══════════════════════════════════════════════════
    // 最終狀態總確認
    // ═══════════════════════════════════════════════════

    @Test
    @Order(15)
    @DisplayName("最終確認｜所有資料最終狀態 + 金額總檢查")
    void step15_最終狀態總確認() throws Exception {

        // 申請單1：張小明 → PACKED，15000
        mockMvc.perform(get("/api/applies/{id}", applyId1))
                .andExpect(jsonPath("$.status").value("PACKED"))
                .andExpect(jsonPath("$.applicantName").value("張小明"))
                .andExpect(jsonPath("$.amount").value(15000));

        // 申請單2：李小花 → PACKED，8000
        mockMvc.perform(get("/api/applies/{id}", applyId2))
                .andExpect(jsonPath("$.status").value("PACKED"))
                .andExpect(jsonPath("$.applicantName").value("李小花"))
                .andExpect(jsonPath("$.amount").value(8000));

        // 傳票 → COMPLETED，加總 23000
        mockMvc.perform(get("/api/vouchers/{id}", voucherId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalAmount").value(23000));

        // 付款單 → COMPLETED，金額 23000
        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(23000));

        System.out.println("✅ 所有狀態驗證通過！");
    }
}
