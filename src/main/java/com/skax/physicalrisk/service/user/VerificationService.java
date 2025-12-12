package com.skax.physicalrisk.service.user;

import com.skax.physicalrisk.domain.user.entity.VerificationCode;
import com.skax.physicalrisk.domain.user.repository.VerificationCodeRepository;
import com.skax.physicalrisk.exception.BusinessException;
import com.skax.physicalrisk.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 이메일 인증 서비스
 *
 * 랜덤 6자리 숫자 인증번호를 생성하여 DB에 저장하고 이메일로 발송합니다.
 * 인증번호는 5분간 유효하며, 만료된 코드는 매일 자정에 자동 정리됩니다.
 *
 * 최종 수정일: 2025-12-12
 * 파일 버전: v03 (랜덤 인증번호 생성)
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

	private final VerificationCodeRepository verificationCodeRepository;
	private final EmailService emailService;
	private static final SecureRandom random = new SecureRandom();

	/**
	 * 6자리 랜덤 인증번호 생성
	 *
	 * @return 6자리 숫자 인증번호 (000000 ~ 999999)
	 */
	public String generateVerificationCode() {
		int code = random.nextInt(1000000); // 0 ~ 999999
		return String.format("%06d", code); // 6자리로 포맷 (앞에 0 채우기)
	}

	/**
	 * 인증번호 발송
	 *
	 * @param email   이메일
	 * @param purpose 인증 목적 (REGISTER, PASSWORD_RESET)
	 */
	@Transactional
	public void sendVerificationEmail(String email, String purpose) {
		log.info("Sending verification email to: {} for purpose: {}", email, purpose);

		// 기존 미인증 코드가 있으면 삭제
		verificationCodeRepository.findTopByEmailAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(email, purpose)
			.ifPresent(existingCode -> {
				log.info("Deleting existing unverified code for email: {}", email);
				verificationCodeRepository.delete(existingCode);
			});

		// 새로운 인증번호 생성 및 저장
		String code = generateVerificationCode();
		VerificationCode verificationCode = VerificationCode.builder()
			.email(email)
			.code(code)
			.purpose(purpose)
			.verified(false)
			.build();

		verificationCodeRepository.save(verificationCode);
		log.info("Verification code saved for email: {}", email);

		// 이메일 발송
		try {
			emailService.sendVerificationCodeEmail(email, code, purpose);
		} catch (Exception e) {
			log.error("Failed to send verification email to: {}", email, e);
			throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED, "이메일 발송에 실패했습니다.");
		}
	}

	/**
	 * 인증번호 검증
	 *
	 * @param email   이메일
	 * @param code    인증번호
	 * @param purpose 인증 목적
	 * @throws BusinessException 인증번호가 유효하지 않거나 만료된 경우
	 */
	@Transactional
	public void verifyCode(String email, String code, String purpose) {
		log.info("Verifying code for email: {} with purpose: {}", email, purpose);

		// 인증번호 조회
		VerificationCode verificationCode = verificationCodeRepository
			.findTopByEmailAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(email, purpose)
			.orElseThrow(() -> {
				log.error("Verification code not found for email: {}", email);
				return new BusinessException(ErrorCode.VERIFICATION_CODE_NOT_FOUND, "유효한 인증번호를 찾을 수 없습니다.");
			});

		// 만료 여부 확인
		if (verificationCode.isExpired()) {
			log.error("Verification code expired for email: {}", email);
			throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED, "인증번호가 만료되었습니다.");
		}

		// 인증번호 일치 여부 확인
		if (!verificationCode.getCode().equals(code)) {
			log.error("Verification code mismatch for email: {}", email);
			throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH, "인증번호가 일치하지 않습니다.");
		}

		// 인증 완료 처리
		verificationCode.verify();
		verificationCodeRepository.save(verificationCode);
		log.info("Verification code verified successfully for email: {}", email);
	}

	/**
	 * 이메일 인증 완료 여부 확인
	 *
	 * @param email   이메일
	 * @param purpose 인증 목적
	 * @return 인증 완료 여부
	 */
	@Transactional(readOnly = true)
	public boolean isEmailVerified(String email, String purpose) {
		return verificationCodeRepository.existsByEmailAndPurposeAndVerifiedTrue(email, purpose);
	}

	/**
	 * 인증 완료 후 인증 코드 삭제
	 *
	 * @param email   이메일
	 * @param purpose 인증 목적
	 */
	@Transactional
	public void clearVerifiedCode(String email, String purpose) {
		log.info("Clearing verified code for email: {} with purpose: {}", email, purpose);
		verificationCodeRepository.deleteByEmailAndPurpose(email, purpose);
	}

	/**
	 * 만료된 인증번호 자동 정리 (매일 자정 실행)
	 */
	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void cleanupExpiredCodes() {
		log.info("Cleaning up expired verification codes");
		LocalDateTime now = LocalDateTime.now();
		verificationCodeRepository.deleteByExpiresAtBefore(now);
		log.info("Expired verification codes cleaned up successfully");
	}
}
