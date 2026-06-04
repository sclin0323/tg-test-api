package com.tg.test.service;

import com.tg.test.data.entity.AccessToken;
import com.tg.test.data.repository.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

    private static final int TOKEN_TTL_MINUTES = 5;

    private final AccessTokenRepository accessTokenRepository;

    @Override
    @Transactional
    public AccessToken generate(String username, String displayName, String email, String role) {
        return accessTokenRepository.save(AccessToken.builder()
                .token(UUID.randomUUID().toString())
                .username(username)
                .displayName(displayName)
                .email(email)
                .role(role)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES))
                .build());
    }

    @Override
    @Transactional
    public AccessToken verify(String token) {
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
