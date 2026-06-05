package com.tg.test.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {

    private static final Map<String, TokenInfo> MOCK_TOKENS = Map.of(
        "ya29.A0ARrdaM-Alice-FinMgr-x8Kz2QpLmNvRtWsYbDcEfGhJiKlMnOpQrStUvWxYz",
            new TokenInfo("ya29.A0ARrdaM-Alice-FinMgr-x8Kz2QpLmNvRtWsYbDcEfGhJiKlMnOpQrStUvWxYz",
                "alice", "Alice 財務主管", "alice@demo.com", "ADMIN"),
        "ya29.A0ARrdaM-Bob-Accountant-p3Hn7FqKjLmNwRsXuVtYzAbCdEfGhIjKlMnOpQrSt",
            new TokenInfo("ya29.A0ARrdaM-Bob-Accountant-p3Hn7FqKjLmNwRsXuVtYzAbCdEfGhIjKlMnOpQrSt",
                "bob", "Bob 會計人員", "bob@demo.com", "FINANCE"),
        "ya29.A0ARrdaM-Carol-Reviewer-m6Gt9EpJiKlNvQtWuXyZaBcDeFgHiJkLmNoPqRsTuV",
            new TokenInfo("ya29.A0ARrdaM-Carol-Reviewer-m6Gt9EpJiKlNvQtWuXyZaBcDeFgHiJkLmNoPqRsTuV",
                "carol", "Carol 審核人員", "carol@demo.com", "REVIEWER")
    );

    @Override
    public TokenInfo generate(String username, String displayName, String email, String role) {
        return MOCK_TOKENS.values().stream()
                .filter(t -> t.username().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到 username 對應的 mock token：" + username));
    }

    @Override
    public TokenInfo verify(String token) {
        if (!MOCK_TOKENS.containsKey(token))
            throw new IllegalArgumentException("Token 不存在");
        return MOCK_TOKENS.get(token);
    }
}
