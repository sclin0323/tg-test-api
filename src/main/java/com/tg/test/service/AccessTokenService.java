package com.tg.test.service;

public interface AccessTokenService {

    record TokenInfo(String token, String account, String name, String email, String role) {}

    TokenInfo generate(String username, String displayName, String email, String role);

    TokenInfo verify(String token);
}
