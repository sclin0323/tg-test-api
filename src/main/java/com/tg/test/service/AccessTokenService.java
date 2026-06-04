package com.tg.test.service;

import com.tg.test.data.entity.AccessToken;

public interface AccessTokenService {

    AccessToken generate(String username, String displayName, String email, String role);

    AccessToken verify(String token);
}
