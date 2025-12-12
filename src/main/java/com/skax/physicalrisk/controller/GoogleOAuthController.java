package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.common.ApiResponse;
import com.skax.physicalrisk.service.oauth.GoogleOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Google OAuth 2.0 컨트롤러
 *
 * Gmail API 사용을 위한 OAuth 2.0 인증
 * - 최초 1회만 관리자가 Google 로그인하여 Refresh Token 발급
 * - Refresh Token은 DB에 영구 저장
 *
 * 파일 버전: v01
 * 최종 수정일: 2025-12-12
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Google OAuth", description = "Gmail API 인증 관리")
public class GoogleOAuthController {

    private final GoogleOAuthService oauthService;

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${google.oauth.auth-uri}")
    private String authUri;

    @Value("${google.oauth.scope}")
    private String scope;

    /**
     * Google OAuth 콜백 처리
     *
     * Google OAuth 인증 후 자동 리다이렉트되는 엔드포인트
     * Authorization Code를 받아서 Access Token + Refresh Token으로 교환하고 DB에 저장
     *
     * @param code Google OAuth Authorization Code
     * @param state CSRF 토큰 (선택)
     * @return 성공 메시지
     */
    @GetMapping("/oauth2callback")
    @Operation(
        summary = "Google OAuth 콜백",
        description = "Google OAuth 인증 후 리다이렉트되는 콜백 엔드포인트. " +
            "Authorization Code를 받아서 Refresh Token을 발급받고 DB에 저장합니다. " +
            "이 엔드포인트는 직접 호출하지 않고, Google OAuth 인증 후 자동으로 호출됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "OAuth 인증 완료",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = "{\"result\": \"success\", \"message\": \"Google OAuth 인증이 완료되었습니다. 이제 Gmail로 이메일을 발송할 수 있습니다.\"}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "code 파라미터 누락",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "토큰 교환 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = "{\"result\": \"error\", \"message\": \"OAuth 인증 코드 교환에 실패했습니다\", \"errorCode\": \"OAUTH_CODE_EXCHANGE_FAILED\"}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Void>> handleOAuthCallback(
        @Parameter(description = "Google OAuth Authorization Code", required = true)
        @RequestParam String code,

        @Parameter(description = "CSRF 토큰 (선택)", required = false)
        @RequestParam(required = false) String state
    ) {
        log.info("Google OAuth 콜백 수신: code={}", code.substring(0, Math.min(20, code.length())) + "...");

        // Authorization Code를 Access Token + Refresh Token으로 교환
        oauthService.exchangeCodeForTokens(code);

        return ResponseEntity.ok(
            ApiResponse.success("Google OAuth 인증이 완료되었습니다. 이제 Gmail로 이메일을 발송할 수 있습니다.")
        );
    }

    /**
     * OAuth 인증 URL 생성 (관리자용)
     *
     * Google OAuth 인증 페이지 URL을 생성합니다.
     * 최초 1회만 사용하며, 반환된 URL을 브라우저에서 열어 Google 로그인 + 권한 승인을 진행합니다.
     *
     * @return OAuth 인증 URL
     */
    @GetMapping("/admin/oauth/authorize")
    @Operation(
        summary = "OAuth 인증 URL 생성",
        description = "Google OAuth 인증 URL을 생성합니다. " +
            "최초 1회만 사용하며, 반환된 authUrl을 브라우저에 붙여넣어 Google 로그인 + 권한 승인을 진행하세요. " +
            "승인 후 자동으로 /oauth2callback으로 리다이렉트되어 Refresh Token이 저장됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "OAuth 인증 URL 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = "{\"result\": \"success\", \"data\": {\"authUrl\": \"https://accounts.google.com/o/oauth2/auth?client_id=...&prompt=consent\"}, \"message\": \"OAuth 인증 URL\"}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Map<String, String>>> getAuthUrl() {
        log.info("OAuth 인증 URL 생성 요청");

        String authUrl = String.format(
            "%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&access_type=offline&prompt=consent",
            authUri, clientId, redirectUri, scope
        );

        Map<String, String> data = Map.of("authUrl", authUrl);

        return ResponseEntity.ok(ApiResponse.success("OAuth 인증 URL", data));
    }
}
