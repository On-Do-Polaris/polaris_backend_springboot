package com.skax.physicalrisk.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인증번호 확인 요청 DTO
 *
 * 최종 수정일: 2025-12-10
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCodeRequest {

	/**
	 * 이메일
	 */
	@NotBlank(message = "이메일은 필수입니다")
	@Email(message = "유효한 이메일 형식이 아닙니다")
	private String email;

	/**
	 * 6자리 인증번호
	 */
	@NotBlank(message = "인증번호는 필수입니다")
	@Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자여야 합니다")
	private String verificationCode;
}
