package com.skax.physicalrisk.domain.oauth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Google OAuth Token 엔티티
 *
 * Gmail API 사용을 위한 OAuth 2.0 토큰 저장
 * - Refresh Token: 영구 저장 (서버 재시작 시에도 유지)
 * - Access Token: 캐싱용 (1시간마다 만료)
 *
 * 파일 버전: v01
 * 최종 수정일: 2025-12-12
 *
 * @author SKAX Team
 */
@Entity
@Table(name = "google_oauth_tokens")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleOAuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Refresh Token (영구 보관)
     * Google OAuth에서 발급받은 Refresh Token
     * 이 토큰으로 Access Token을 주기적으로 갱신
     */
    @Column(nullable = false, length = 500)
    private String refreshToken;

    /**
     * Access Token (캐시)
     * 실제 Gmail API 호출 시 사용
     * 1시간마다 만료되므로 주기적으로 갱신 필요
     */
    @Column(length = 500)
    private String accessToken;

    /**
     * Token Type (보통 "Bearer")
     */
    @Column(length = 20)
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access Token 만료 시간
     * 이 시간 이전에 Refresh Token으로 갱신 필요
     */
    private LocalDateTime expiresAt;

    /**
     * OAuth Scope
     * Gmail 발송 권한
     */
    @Column(length = 255)
    @Builder.Default
    private String scope = "https://www.googleapis.com/auth/gmail.send";

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Access Token 업데이트
     *
     * @param accessToken 새로운 Access Token
     * @param expiresAt 만료 시간
     */
    public void updateAccessToken(String accessToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
        this.updatedAt = LocalDateTime.now();
    }
}
