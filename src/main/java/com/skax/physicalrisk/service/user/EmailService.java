package com.skax.physicalrisk.service.user;

import com.skax.physicalrisk.client.gmail.GmailClient;
import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
	private final UserRepository userRepository;

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
		String link = "https://on-do.site/privacy-policy";

		if ("REGISTER".equals(purpose)) {
			subject = "[SKAX-Polaris] 이메일 인증 코드 안내";
			emailContent = String.format(
				"안녕하세요, %s님\n\n" +
				"SKAX-Polaris 사업장 기후 물리적 리스크 AI 평가 시스템 계정 인증을 위한 일회용 코드를 안내드립니다.\n\n" +
				"인증 코드: %s\n\n" +
				"본 코드는 발송 시점으로부터 5분간 유효하며, 웹 페이지에서 입력하시면 인증이 완료됩니다.\n\n" +
				"보안 유의사항\n" +
				" - 본 인증 코드는 제3자와 절대 공유하지 마십시오.\n" +
				" - 본인이 요청하지 않은 경우, 즉시 비밀번호를 변경해 주시기 바랍니다. \n" +
				" - SKALA On-Do는 이메일을 통해서만 인증 코드를 발송하며, 다른 경로로 코드를 요청하는 경우 사칭 사기이니 주의하시기 바랍니다.\n\n" +
				"본 메일은 본인 확인을 위해 자동으로 발송됩니다.\n\n" +
				"감사합니다.\n\n" +
				"SKALA On-Do 팀\n" +
				"SKAX-Polaris: 사업장 기후 물리적 리스크 AI 평가 시스템\n\n" +
				"개인정보처리방침: %s",
				toEmail, code, link
			);
		} else if ("PASSWORD_RESET".equals(purpose)) {
			subject = "[SKAX-Polaris] 비밀번호 재설정 인증번호";
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
			subject = "[SKAX-Polaris] 인증번호";
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

		log.info("인증번호 이메일 발송 완료: to={}, purpose={}", toEmail, purpose);
	}

	/**
	 * 분석 완료 이메일 발송 (이메일 주소로)
	 *
	 * @param toEmail 수신자 이메일
	 */
	public void sendAnalysisCompletionEmail(String toEmail) {
		log.info("분석 완료 이메일 발송: to={}", toEmail);

		String link = "https://on-do.site/privacy-policy";
		String subject = "[SKAX-Polaris] 사업장 분석 완료!";
		String emailContent = String.format(
				"안녕하세요, %s님\n\n" +
				"SKAX-Polaris 사업장 기후 물리적 리스크 AI 평가 시스템입니다.\n\n" +
				"요청하신 사업장에 대한 기후 물리적 리스크 분석이 완료되었습니다.\n\n" +
				"분석 완료 정보\n" +
				" - 분석 완료 시각: 2025년 12월 18일 22:15 KST\n" +
				" - 분석 대상: [사업장 개수] \n" +
				" - 평가 항목: [9대 재해 리스크]\n" + //
				" - 분석 결과: [상세 분석 결과]\n\n" +
				"이제 Polaris 시스템에서 상세 분석 결과를 확인하실 수 있습니다.\n\n" +
				"[SKAX-Polaris 주소]\n" +
				"https://on-do.site\n\n" +
				"[주요 제공 정보]\n" +
				" - 사업장별 기후 물리적 리스크 평가 결과\n" +
				" - 시나리오별 물리적 리스크 및 연평균 재무 손실률(AAL) 분석\n" +
				" - 시계열 리스크 변화 추이\n" +
				" - 위치 및 기후 시뮬레이션\n" +
				" - TCFD 보고서\n\n" +
				"※ 분석 결과는 로그인 후 대시보드에서 확인 가능합니다.\n\n" +
				"문의사항이 있으시면 언제든지 연락 주시기 바랍니다.\n\n" +
				"감사합니다.\n\n" +
				"SKALA On-Do 팀\n" +
				"SKAX-Polaris: 사업장 기후 물리적 리스크 AI 평가 시스템\n\n" +
				"개인정보처리방침: %s",
				toEmail, link
			);
		// Gmail API를 통한 실제 이메일 발송
		gmailClient.sendEmail(toEmail, subject, emailContent);

		log.info("분석 완료 이메일 발송 완료: to={}", toEmail);
	}

	/**
	 * 분석 완료 이메일 발송 (사용자 UUID로)
	 *
	 * @param userId 사용자 UUID
	 * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
	 */
	public void sendAnalysisCompletionEmail(UUID userId) {
		log.info("분석 완료 이메일 발송 요청: userId={}", userId);

		// UUID로 사용자 조회
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// 이메일 발송
		sendAnalysisCompletionEmail(user.getEmail());
	}

}
