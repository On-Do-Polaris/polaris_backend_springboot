package com.skax.physicalrisk.domain.user.repository;

import com.skax.physicalrisk.domain.user.entity.RefreshToken;
import com.skax.physicalrisk.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Refresh Token 레포지토리
 *
 * 최종 수정일: 2025-12-03
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	/**
	 * 토큰 값으로 조회
	 *
	 * @param token 토큰 값
	 * @return RefreshToken Optional
	 */
	Optional<RefreshToken> findByToken(String token);

	/**
	 * 사용자의 모든 토큰 조회
	 *
	 * @param user 사용자
	 * @return RefreshToken 리스트
	 */
	java.util.List<RefreshToken> findByUser(User user);

	/**
	 * 사용자의 모든 토큰 폐기
	 *
	 * @param user 사용자
	 */
	@Modifying
	@Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user AND rt.revoked = false")
	void revokeAllByUser(@Param("user") User user);

	/**
	 * 만료된 토큰 삭제
	 *
	 * @param now 현재 시간
	 * @return 삭제된 토큰 수
	 */
	@Modifying
	@Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
	int deleteExpiredTokens(@Param("now") LocalDateTime now);

	/**
	 * 폐기된 토큰 삭제
	 *
	 * @return 삭제된 토큰 수
	 */
	@Modifying
	@Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true")
	int deleteRevokedTokens();
}
