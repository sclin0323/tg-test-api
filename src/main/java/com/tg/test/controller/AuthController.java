package com.tg.test.controller;

import com.tg.test.data.entity.AccessToken;
import com.tg.test.service.AccessTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth 驗證作業")
public class AuthController {

    private final AccessTokenService accessTokenService;

    @Operation(summary = "產生 Access Token（主系統呼叫）")
    @PostMapping("/token")
    public ResponseEntity<GenerateResponse> generate(@RequestBody GenerateRequest req) {
        AccessToken token = accessTokenService.generate(
                req.username(), req.displayName(), req.email(), req.role());
        return ResponseEntity.ok(new GenerateResponse(token.getToken(), token.getExpiresAt()));
    }

    @Operation(summary = "驗證 Access Token（APEX 呼叫）")
    @GetMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(@RequestParam String token) {
        AccessToken accessToken = accessTokenService.verify(token);
        return ResponseEntity.ok(new VerifyResponse(
                accessToken.getUsername(),
                accessToken.getDisplayName(),
                accessToken.getEmail(),
                accessToken.getRole()
        ));
    }

    public record GenerateRequest(String username, String displayName, String email, String role) {}
    public record GenerateResponse(String token, LocalDateTime expiresAt) {}
    public record VerifyResponse(String username, String displayName, String email, String role) {}
}
