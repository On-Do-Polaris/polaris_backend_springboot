package com.skax.physicalrisk.domain.user.repository;

import com.skax.physicalrisk.domain.user.entity.PasswordResetToken;
import com.skax.physicalrisk.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 비밀번호 재설정 토큰 레포지토리
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

	/**
	 * 토큰으로 조회
	 *
	 * @param token 토큰
	 * @return 토큰 Optional
	 */
	Optional<PasswordResetToken> findByToken(String token);

	/**
	 * 사용자의 미사용 유효 토큰 조회
	 *
	 * @param user 사용자
	 * @param now 현재 시간
	 * @return 토큰 Optional
	 */
	Optional<PasswordResetToken> findByUserAndUsedFalseAndExpiresAtAfter(User user, LocalDateTime now);

	/**
	 * 만료된 토큰 삭제
	 *
	 * @param now 현재 시간
	 */
	void deleteByExpiresAtBefore(LocalDateTime now);
}
