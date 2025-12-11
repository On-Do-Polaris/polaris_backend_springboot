package com.skax.physicalrisk.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 이메일 서비스 (더미 구현)
 *
 * SMTP 대신 로그로 이메일 내용을 출력합니다.
 *
 * 최종 수정일: 2025-12-11
 * 파일 버전: v02 (더미 구현)
 *
 * @author SKAX Team
 */
@Slf4j
@Service
public class EmailService {

	/**
	 * 인증번호 이메일 발송 (더미 구현)
	 *
	 * @param toEmail 수신자 이메일
	 * @param code    6자리 인증번호
	 * @param purpose 인증 목적 (REGISTER, PASSWORD_RESET)
	 */
	public void sendVerificationCodeEmail(String toEmail, String code, String purpose) {
		log.info("=".repeat(80));
		log.info("[DUMMY EMAIL] Sending verification code email");
		log.info("To: {}", toEmail);
		log.info("Purpose: {}", purpose);
		log.info("Verification Code: {}", code);

		String subject;
		String emailContent;

		if ("REGISTER".equals(purpose)) {
			subject = "[SKAX] 회원가입 인증번호";
			emailContent = String.format(
				"안녕하세요,\n\n" +
				"회원가입을 위한 인증번호입니다.\n\n" +
				"인증번호: %s\n\n" +
				"이 인증번호는 5분 동안 유효합니다.\n\n" +
				"본인이 요청하지 않았다면 이 이메일을 무시하셔도 됩니다.\n\n" +
				"감사합니다.",
				code
			);
		} else if ("PASSWORD_RESET".equals(purpose)) {
			subject = "[SKAX] 비밀번호 재설정 인증번호";
			emailContent = String.format(
				"안녕하세요,\n\n" +
				"비밀번호 재설정을 위한 인증번호입니다.\n\n" +
				"인증번호: %s\n\n" +
				"이 인증번호는 5분 동안 유효합니다.\n\n" +
				"본인이 요청하지 않았다면 이 이메일을 무시하셔도 됩니다.\n\n" +
				"감사합니다.",
				code
			);
		} else {
			subject = "[SKAX] 인증번호";
			emailContent = String.format(
				"안녕하세요,\n\n" +
				"인증번호: %s\n\n" +
				"이 인증번호는 5분 동안 유효합니다.\n\n" +
				"감사합니다.",
				code
			);
		}

		log.info("Subject: {}", subject);
		log.info("Content:\n{}", emailContent);
		log.info("=".repeat(80));
		log.info("✅ Verification code email sent successfully (DUMMY MODE)");
	}

	/**
	 * 비밀번호 재설정 이메일 발송 (더미 구현)
	 *
	 * @param toEmail 수신자 이메일
	 * @param resetToken 재설정 토큰
	 */
	public void sendPasswordResetEmail(String toEmail, String resetToken) {
		log.info("=".repeat(80));
		log.info("[DUMMY EMAIL] Sending password reset email");
		log.info("To: {}", toEmail);
		log.info("Reset Token: {}", resetToken);

		String subject = "비밀번호 재설정 요청";
		String resetUrl = "http://localhost:3000/reset-password?token=" + resetToken;
		String emailContent = String.format(
			"안녕하세요,\n\n" +
			"비밀번호 재설정을 요청하셨습니다.\n\n" +
			"아래 링크를 클릭하여 비밀번호를 재설정해주세요:\n" +
			"%s\n\n" +
			"이 링크는 30분 동안 유효합니다.\n\n" +
			"본인이 요청하지 않았다면 이 이메일을 무시하셔도 됩니다.\n\n" +
			"감사합니다.",
			resetUrl
		);

		log.info("Subject: {}", subject);
		log.info("Content:\n{}", emailContent);
		log.info("=".repeat(80));
		log.info("✅ Password reset email sent successfully (DUMMY MODE)");
	}
}
