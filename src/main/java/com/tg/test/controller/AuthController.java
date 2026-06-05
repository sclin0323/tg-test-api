package com.tg.test.controller;

import com.tg.test.service.AccessTokenService;
import com.tg.test.service.TokenInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth 驗證作業")
public class AuthController {

    private final AccessTokenService accessTokenService;

    @Operation(summary = "產生 Access Token")
    @PostMapping("/token")
    public ResponseEntity<TokenInfo> generate(@RequestBody GenerateRequest req) {
        return ResponseEntity.ok(
                accessTokenService.generate(req.username(), req.displayName(), req.email(), req.role()));
    }

    @Operation(summary = "驗證 Access Token")
    @GetMapping("/verify")
    public ResponseEntity<TokenInfo> verify(@RequestParam String token) {
        return ResponseEntity.ok(accessTokenService.verify(token));
    }

    public record GenerateRequest(String username, String displayName, String email, String role) {}
}
