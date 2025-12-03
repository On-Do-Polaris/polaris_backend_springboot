package com.skax.physicalrisk.config;

import com.skax.physicalrisk.domain.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 토큰 정리 스케줄러
 *
 * 최종 수정일: 2025-12-03
 * 파일 버전: v01
 *
 * 만료되거나 폐기된 Refresh Token을 주기적으로 정리
 *
 * @author SKAX Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

	private final RefreshTokenRepository refreshTokenRepository;

	/**
	 * 만료된 토큰 정리
	 * 매일 새벽 3시에 실행
	 */
	@Scheduled(cron = "0 0 3 * * ?")
	@Transactional
	public void cleanupExpiredTokens() {
		log.info("Starting cleanup of expired refresh tokens");

		int deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());

		log.info("Cleanup completed. Deleted {} expired refresh tokens", deletedCount);
	}

	/**
	 * 폐기된 토큰 정리
	 * 매주 일요일 새벽 4시에 실행
	 */
	@Scheduled(cron = "0 0 4 ? * SUN")
	@Transactional
	public void cleanupRevokedTokens() {
		log.info("Starting cleanup of revoked refresh tokens");

		int deletedCount = refreshTokenRepository.deleteRevokedTokens();

		log.info("Cleanup completed. Deleted {} revoked refresh tokens", deletedCount);
	}
}
