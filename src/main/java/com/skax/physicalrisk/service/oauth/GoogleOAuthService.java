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
 * - Refresh Token을 사용한 Access Token 갱신
 * - DB에 저장된 Refresh Token 사용
 *
 * 파일 버전: v02 (토큰 교환 기능 제거 - 수동 설정)
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

    @Value("${google.oauth.token-uri}")
    private String tokenUri;

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

}
