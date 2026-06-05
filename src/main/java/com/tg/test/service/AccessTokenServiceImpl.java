package com.tg.test.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {

    private static final Map<String, TokenInfo> MOCK_TOKENS = Map.of(
        "ya29.A0ARrdaM-SAM-LinShangChun-x8Kz2QpLmNvRtWsYbDcEfGhJiKlMnOpQrStUvWxYz",
            new TokenInfo("ya29.A0ARrdaM-SAM-LinShangChun-x8Kz2QpLmNvRtWsYbDcEfGhJiKlMnOpQrStUvWxYz",
                "SAM", "林上淳", "sam@demo.com", "ADMIN"),
        "ya29.A0ARrdaM-Lynn-WangJunLing-p3Hn7FqKjLmNwRsXuVtYzAbCdEfGhIjKlMnOpQrSt",
            new TokenInfo("ya29.A0ARrdaM-Lynn-WangJunLing-p3Hn7FqKjLmNwRsXuVtYzAbCdEfGhIjKlMnOpQrSt",
                "LYNN", "王俊翎", "lynn@demo.com", "FINANCE"),
        "ya29.A0ARrdaM-Powei-ChangBoWei-m6Gt9EpJiKlNvQtWuXyZaBcDeFgHiJkLmNoPqRsTuV",
            new TokenInfo("ya29.A0ARrdaM-Powei-ChangBoWei-m6Gt9EpJiKlNvQtWuXyZaBcDeFgHiJkLmNoPqRsTuV",
                "POWEI", "張博崴", "powei@demo.com", "REVIEWER")
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
