package com.skax.physicalrisk.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Google OAuth Callback Controller
 *
 * OAuth 인증 후 리다이렉트되는 엔드포인트
 * 토큰 교환은 수동으로 처리
 *
 * 파일 버전: v02 (자동 토큰 교환 제거)
 * 최종 수정일: 2025-12-12
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@Hidden  // Swagger에서 숨김
public class GoogleOAuthController {

    /**
     * Google OAuth 콜백 엔드포인트
     *
     * @param code  Authorization Code (Google에서 발급)
     * @param state 상태값 (CSRF 방지용)
     * @return Authorization Code 전체 출력
     */
    @GetMapping("/oauth2callback")
    public ResponseEntity<String> handleOAuthCallback(
        @RequestParam String code,
        @RequestParam(required = false) String state
    ) {
        log.info("=".repeat(80));
        log.info("Google OAuth Authorization Code 수신:");
        log.info("Code: {}", code);
        log.info("State: {}", state);
        log.info("=".repeat(80));
        log.info("다음 단계: 수동으로 토큰 교환 실행 필요");
        log.info("=".repeat(80));

        // 화면에도 전체 코드 출력
        String response = String.format(
            "Google OAuth Authorization Code:\n\n%s\n\n" +
            "State: %s\n\n" +
            "다음 단계: 이 코드를 사용하여 수동으로 토큰 교환을 진행하세요.",
            code, state
        );

        return ResponseEntity.ok(response);
    }
}
