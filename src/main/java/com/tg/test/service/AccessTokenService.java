package com.tg.test.service;

public interface AccessTokenService {

    TokenInfo generate(String username, String displayName, String email, String role);

    TokenInfo verify(String token);
}
