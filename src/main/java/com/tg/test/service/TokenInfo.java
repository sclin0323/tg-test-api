package com.tg.test.service;

public record TokenInfo(
        String token,
        String username,
        String displayName,
        String email,
        String role
) {}
