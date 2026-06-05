package com.tg.test.service;

import com.tg.test.data.entity.AccessToken;
import com.tg.test.data.repository.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

    // ── Demo 用：寫死 3 組 token，不碰 DB ──────────────────────────────
    private static final Map<String, AccessToken> MOCK_TOKENS = Map.of(
        "ya29.A0ARrdaM-Alice-FinMgr-x8Kz2QpLmNvRtWsYbDcEfGhJiKlMnOpQrStUvWxYz", AccessToken.builder()
            .token("ya29.A0ARrdaM-Alice-FinMgr-x8Kz2QpLmNvRtWsYbDcEfGhJiKlMnOpQrStUvWxYz")
            .username("alice").displayName("Alice 財務主管").email("alice@demo.com").role("ADMIN")
            .expiresAt(LocalDateTime.now().plusYears(10)).build(),
        "ya29.A0ARrdaM-Bob-Accountant-p3Hn7FqKjLmNwRsXuVtYzAbCdEfGhIjKlMnOpQrSt", AccessToken.builder()
            .token("ya29.A0ARrdaM-Bob-Accountant-p3Hn7FqKjLmNwRsXuVtYzAbCdEfGhIjKlMnOpQrSt")
            .username("bob").displayName("Bob 會計人員").email("bob@demo.com").role("FINANCE")
            .expiresAt(LocalDateTime.now().plusYears(10)).build(),
        "ya29.A0ARrdaM-Carol-Reviewer-m6Gt9EpJiKlNvQtWuXyZaBcDeFgHiJkLmNoPqRsTuV", AccessToken.builder()
            .token("ya29.A0ARrdaM-Carol-Reviewer-m6Gt9EpJiKlNvQtWuXyZaBcDeFgHiJkLmNoPqRsTuV")
            .username("carol").displayName("Carol 審核人員").email("carol@demo.com").role("REVIEWER")
            .expiresAt(LocalDateTime.now().plusYears(10)).build()
    );
    // ──────────────────────────────────────────────────────────────────

    private final AccessTokenRepository accessTokenRepository;

    @Override
    @Transactional
    public AccessToken generate(String username, String displayName, String email, String role) {
        // 若傳入的 username 有對應的 mock token，直接回傳，不存 DB
        return MOCK_TOKENS.values().stream()
                .filter(t -> t.getUsername().equals(username))
                .findFirst()
                .orElseGet(() -> accessTokenRepository.save(AccessToken.builder()
                        .token(java.util.UUID.randomUUID().toString())
                        .username(username).displayName(displayName)
                        .email(email).role(role)
                        .expiresAt(LocalDateTime.now().plusMinutes(5))
                        .build()));
    }

    @Override
    @Transactional
    public AccessToken verify(String token) {
        // 先查 mock，找到就直接回傳，不查 DB
        if (MOCK_TOKENS.containsKey(token)) {
            return MOCK_TOKENS.get(token);
        }

        AccessToken accessToken = accessTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token 不存在"));

        if (accessToken.isUsed())
            throw new IllegalStateException("Token 已使用");

        if (LocalDateTime.now().isAfter(accessToken.getExpiresAt()))
            throw new IllegalStateException("Token 已過期");

        accessToken.setUsed(true);
        return accessTokenRepository.save(accessToken);
    }
}
