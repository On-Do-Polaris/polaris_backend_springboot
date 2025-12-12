package com.skax.physicalrisk.domain.oauth.repository;

import com.skax.physicalrisk.domain.oauth.entity.GoogleOAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Google OAuth Token Repository
 *
 * Gmail API 사용을 위한 OAuth 토큰 저장소
 *
 * 파일 버전: v01
 * 최종 수정일: 2025-12-12
 *
 * @author SKAX Team
 */
@Repository
public interface GoogleOAuthTokenRepository extends JpaRepository<GoogleOAuthToken, UUID> {

    /**
     * 최신 OAuth 토큰 조회
     * 서버는 단일 Gmail 계정을 사용하므로 가장 최근 업데이트된 토큰을 반환
     *
     * @return 최신 OAuth 토큰
     */
    Optional<GoogleOAuthToken> findFirstByOrderByUpdatedAtDesc();
}
