package com.skax.physicalrisk.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 이메일 서비스
 *
 * 최종 수정일: 2025-12-03
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.mail.host")
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Value("${app.frontend.url:http://localhost:3000}")
	private String frontendUrl;

	/**
	 * 비밀번호 재설정 이메일 발송
	 *
	 * @param toEmail 수신자 이메일
	 * @param resetToken 재설정 토큰
	 */
	public void sendPasswordResetEmail(String toEmail, String resetToken) {
		log.info("Sending password reset email to: {}", toEmail);

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail);
			message.setTo(toEmail);
			message.setSubject("비밀번호 재설정 요청");

			String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
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

			message.setText(emailContent);

			mailSender.send(message);

			log.info("Password reset email sent successfully to: {}", toEmail);
		} catch (Exception e) {
			log.error("Failed to send password reset email to: {}", toEmail, e);
			throw new RuntimeException("이메일 발송에 실패했습니다.", e);
		}
	}
}
