package com.skax.physicalrisk.domain.user.repository;

import com.skax.physicalrisk.domain.user.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 이메일 인증 코드 Repository
 *
 * 최종 수정일: 2025-12-10
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {

	/**
	 * 이메일과 목적으로 가장 최근 미인증 코드 조회
	 *
	 * @param email   이메일
	 * @param purpose 인증 목적
	 * @return 인증 코드
	 */
	Optional<VerificationCode> findTopByEmailAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(String email, String purpose);

	/**
	 * 만료된 인증 코드 삭제
	 *
	 * @param now 현재 시간
	 */
	void deleteByExpiresAtBefore(LocalDateTime now);

	/**
	 * 이메일과 목적으로 인증 완료된 코드 존재 여부 확인
	 *
	 * @param email   이메일
	 * @param purpose 인증 목적
	 * @return 인증 완료된 코드가 존재하면 true
	 */
	boolean existsByEmailAndPurposeAndVerifiedTrue(String email, String purpose);

	/**
	 * 이메일과 목적으로 인증 완료된 코드 조회
	 *
	 * @param email   이메일
	 * @param purpose 인증 목적
	 * @return 인증 완료된 코드
	 */
	Optional<VerificationCode> findTopByEmailAndPurposeAndVerifiedTrueOrderByCreatedAtDesc(String email, String purpose);

	/**
	 * 이메일과 목적으로 모든 인증 코드 삭제
	 *
	 * @param email   이메일
	 * @param purpose 인증 목적
	 */
	void deleteByEmailAndPurpose(String email, String purpose);
}
