package com.tg.test.service;

public interface AccessTokenService {

    record TokenInfo(String token, String username, String displayName, String email, String role) {}

    TokenInfo generate(String username, String displayName, String email, String role);

    TokenInfo verify(String token);
}
