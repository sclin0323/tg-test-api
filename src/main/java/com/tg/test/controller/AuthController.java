package com.tg.test.controller;

import com.tg.test.service.AccessTokenService;
import com.tg.test.service.AccessTokenService.TokenInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth 驗證作業")
public class AuthController {

    private final AccessTokenService accessTokenService;

    @Operation(summary = "產生 Access Token")
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> generate(@RequestBody GenerateRequest req) {
        TokenInfo info = accessTokenService.generate(req.account(), req.name(), req.email(), req.role());
        return ResponseEntity.ok(successResponse(Map.of(
                "token", info.token(),
                "account", info.account(),
                "name", info.name()
        )));
    }

    @Operation(summary = "驗證 Access Token")
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            return ResponseEntity.badRequest().body(Map.of("statusCode", 400, "message", "缺少 Bearer token"));

        String token = authorization.substring(7); // 去掉 "Bearer "
        TokenInfo info = accessTokenService.verify(token);
        return ResponseEntity.ok(successResponse(Map.of(
                "account", info.account(),
                "name", info.name()
        )));
    }

    // ── 統一回傳格式 ──────────────────────────────────────────────────
    private Map<String, Object> successResponse(Object data) {
        return Map.of(
                "systemTime", System.currentTimeMillis(),
                "statusCode", 200,
                "message", "SUCCESS",
                "data", data
        );
    }

    public record GenerateRequest(String account, String name, String email, String role) {}
}
