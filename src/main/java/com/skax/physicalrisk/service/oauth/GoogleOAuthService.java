package com.skax.physicalrisk.service.oauth;

import com.skax.physicalrisk.domain.oauth.entity.GoogleOAuthToken;
import com.skax.physicalrisk.domain.oauth.repository.GoogleOAuthTokenRepository;
import com.skax.physicalrisk.exception.BusinessException;
import com.skax.physicalrisk.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Google OAuth 2.0 서비스
 *
 * Gmail API 사용을 위한 OAuth 2.0 토큰 관리
 * - Authorization Code를 Access Token + Refresh Token으로 교환
 * - Refresh Token을 사용한 Access Token 갱신
 * - DB에 Refresh Token 영구 저장
 *
 * 파일 버전: v01
 * 최종 수정일: 2025-12-12
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final GoogleOAuthTokenRepository tokenRepository;
    private final WebClient webClient = WebClient.create();

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${google.oauth.token-uri}")
    private String tokenUri;

    /**
     * Authorization Code를 Access Token + Refresh Token으로 교환
     *
     * Google OAuth 인증 후 받은 code를 사용하여 토큰을 발급받고 DB에 저장
     *
     * @param code Authorization Code
     */
    @Transactional
    public void exchangeCodeForTokens(String code) {
        log.info("Google OAuth 토큰 교환 시작: code={}", code.substring(0, Math.min(20, code.length())) + "...");

        try {
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("code", code);
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);
            requestBody.add("redirect_uri", redirectUri);
            requestBody.add("grant_type", "authorization_code");

            Map<String, Object> response = webClient.post()
                .uri(tokenUri)
                .body(BodyInserters.fromFormData(requestBody))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

            if (response == null) {
                throw new BusinessException(ErrorCode.OAUTH_CODE_EXCHANGE_FAILED, "토큰 교환 응답이 null입니다");
            }

            String accessToken = (String) response.get("access_token");
            String refreshToken = (String) response.get("refresh_token");
            Integer expiresIn = (Integer) response.get("expires_in");

            if (accessToken == null || refreshToken == null) {
                log.error("토큰 교환 응답에 필수 필드 누락: {}", response);
                throw new BusinessException(ErrorCode.OAUTH_CODE_EXCHANGE_FAILED, "Access Token 또는 Refresh Token이 없습니다");
            }

            GoogleOAuthToken token = GoogleOAuthToken.builder()
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .expiresAt(LocalDateTime.now().plusSeconds(expiresIn != null ? expiresIn : 3600))
                .build();

            tokenRepository.save(token);
            log.info("Google OAuth 토큰 저장 완료: tokenId={}", token.getId());

        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("Google OAuth 토큰 교환 실패: status={}, body={}", e.getStatusCode(), errorBody);
            log.error("요청 파라미터: clientId={}, redirectUri={}, grantType=authorization_code",
                clientId, redirectUri);
            throw new BusinessException(ErrorCode.OAUTH_CODE_EXCHANGE_FAILED,
                "토큰 교환 실패 [" + e.getStatusCode() + "]: " + errorBody);
        } catch (Exception e) {
            log.error("Google OAuth 토큰 교환 중 예외 발생", e);
            throw new BusinessException(ErrorCode.OAUTH_CODE_EXCHANGE_FAILED, e.getMessage());
        }
    }

    /**
     * 유효한 Access Token 반환
     *
     * DB에서 Access Token을 조회하고, 만료되었다면 Refresh Token으로 갱신
     *
     * @return 유효한 Access Token
     */
    @Transactional
    public String getValidAccessToken() {
        GoogleOAuthToken token = tokenRepository.findFirstByOrderByUpdatedAtDesc()
            .orElseThrow(() -> new BusinessException(
                ErrorCode.OAUTH_TOKEN_NOT_FOUND,
                "OAuth 토큰이 없습니다. 관리자가 먼저 Google OAuth 인증을 완료해야 합니다"
            ));

        // Access Token이 5분 이내에 만료되면 갱신
        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(5))) {
            log.info("Access Token 만료 또는 만료 임박. 갱신 시작: tokenId={}", token.getId());
            refreshAccessToken(token);
        }

        return token.getAccessToken();
    }

    /**
     * Refresh Token을 사용하여 Access Token 갱신
     *
     * @param token 갱신할 GoogleOAuthToken 엔티티
     */
    private void refreshAccessToken(GoogleOAuthToken token) {
        log.info("Refresh Token으로 Access Token 갱신 시작");

        try {
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("refresh_token", token.getRefreshToken());
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);
            requestBody.add("grant_type", "refresh_token");

            Map<String, Object> response = webClient.post()
                .uri(tokenUri)
                .body(BodyInserters.fromFormData(requestBody))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

            if (response == null) {
                throw new BusinessException(ErrorCode.OAUTH_TOKEN_REFRESH_FAILED, "토큰 갱신 응답이 null입니다");
            }

            String newAccessToken = (String) response.get("access_token");
            Integer expiresIn = (Integer) response.get("expires_in");

            if (newAccessToken == null) {
                log.error("토큰 갱신 응답에 access_token 누락: {}", response);
                throw new BusinessException(ErrorCode.OAUTH_TOKEN_REFRESH_FAILED, "새 Access Token이 없습니다");
            }

            LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(expiresIn != null ? expiresIn : 3600);
            token.updateAccessToken(newAccessToken, newExpiresAt);
            tokenRepository.save(token);

            log.info("Access Token 갱신 완료: 만료시간={}", newExpiresAt);

        } catch (WebClientResponseException e) {
            log.error("Access Token 갱신 실패: status={}, body={}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_REFRESH_FAILED,
                "토큰 갱신 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("Access Token 갱신 중 예외 발생", e);
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_REFRESH_FAILED, e.getMessage());
        }
    }

    /**
     * OAuth 인증 URL 생성 (관리자용)
     *
     * @return OAuth 인증 URL
     */
    public String generateAuthUrl() {
        // authUri와 scope는 클래스 필드에 이미 선언되어 있지만,
        // GoogleOAuthController에서 직접 사용하므로 이 메서드는 사용되지 않음
        String authUriLocal = "https://accounts.google.com/o/oauth2/auth";
        String scopeLocal = "https://www.googleapis.com/auth/gmail.send";

        return String.format(
            "%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&access_type=offline&prompt=consent",
            authUriLocal, clientId, redirectUri, scopeLocal
        );
    }
}
