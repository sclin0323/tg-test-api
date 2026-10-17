package com.tg.test.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {

    private static final Map<String, TokenInfo> MOCK_TOKENS = Map.of(
        "ya29.A0ARrdaM-SAM-LinShangChun-x8Kz2QpLmNvRtWsYbDcEfGhJiKlMnOpQrStUvWxYz",
            new TokenInfo("ya29.A0ARrdaM-SAM-LinShangChun-x8Kz2QpLmNvRtWsYbDcEfGhJiKlMnOpQrStUvWxYz",
                "003512", "林上淳", "sam@demo.com", "ADMIN"),
        "ya29.A0ARrdaM-Lynn-WangJunLing-p3Hn7FqKjLmNwRsXuVtYzAbCdEfGhIjKlMnOpQrSt",
            new TokenInfo("ya29.A0ARrdaM-Lynn-WangJunLing-p3Hn7FqKjLmNwRsXuVtYzAbCdEfGhIjKlMnOpQrSt",
                "003513", "王俊翎", "lynn@demo.com", "FINANCE"),
        "ya29.A0ARrdaM-Powei-ChangBoWei-m6Gt9EpJiKlNvQtWuXyZaBcDeFgHiJkLmNoPqRsTuV",
            new TokenInfo("ya29.A0ARrdaM-Powei-ChangBoWei-m6Gt9EpJiKlNvQtWuXyZaBcDeFgHiJkLmNoPqRsTuV",
                "003514", "張博崴", "powei@demo.com", "REVIEWER")
    );

    @Override
    public TokenInfo generate(String username, String displayName, String email, String role) {
        return MOCK_TOKENS.values().stream()
                .filter(t -> t.account().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到 account 對應的 mock token：" + username));
    }

    @Override
    public TokenInfo verify(String token) {
        if (!MOCK_TOKENS.containsKey(token))
            throw new IllegalArgumentException("Token 不存在");
        return MOCK_TOKENS.get(token);
    }
}
