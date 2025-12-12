package com.skax.physicalrisk.service.user;

import com.skax.physicalrisk.client.gmail.GmailClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 이메일 서비스
 *
 * Gmail API를 사용한 실제 이메일 발송
 *
 * 최종 수정일: 2025-12-12
 * 파일 버전: v03 (Gmail API 연동)
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final GmailClient gmailClient;

	/**
	 * 인증번호 이메일 발송
	 *
	 * @param toEmail 수신자 이메일
	 * @param code    6자리 인증번호
	 * @param purpose 인증 목적 (REGISTER, PASSWORD_RESET)
	 */
	public void sendVerificationCodeEmail(String toEmail, String code, String purpose) {
		log.info("인증번호 이메일 발송: to={}, purpose={}", toEmail, purpose);

		String subject;
		String emailContent;

		if ("REGISTER".equals(purpose)) {
			subject = "[SKAX On-Do] 회원가입 인증번호";
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
			subject = "[SKAX On-Do] 비밀번호 재설정 인증번호";
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
			subject = "[SKAX On-Do] 인증번호";
			emailContent = String.format(
				"안녕하세요,\n\n" +
				"인증번호: %s\n\n" +
				"이 인증번호는 5분 동안 유효합니다.\n\n" +
				"감사합니다.",
				code
			);
		}

		// Gmail API를 통한 실제 이메일 발송
		gmailClient.sendEmail(toEmail, subject, emailContent);

		log.info("✅ 인증번호 이메일 발송 완료: to={}, purpose={}", toEmail, purpose);
	}

	/**
	 * 분석 완료 이메일 발송
	 *
	 * @param toEmail 수신자 이메일
	 */
	public void sendAnalysisCompletionEmail(String toEmail) {
		log.info("분석 완료 이메일 발송: to={}", toEmail);

		String subject = "[SKAX On-Do] 사업장 분석 완료";
		String emailContent = "안녕하세요,\n\n" +
			"요청하신 사업장 물리적 리스크 분석이 완료되었습니다.\n\n" +
			"SKAX On-Do에 로그인하여 분석 결과를 확인하실 수 있습니다.\n\n" +
			"감사합니다.\n\n" +
			"SKAX On-Do 팀";

		// Gmail API를 통한 실제 이메일 발송
		gmailClient.sendEmail(toEmail, subject, emailContent);

		log.info("✅ 분석 완료 이메일 발송 완료: to={}", toEmail);
	}

}
