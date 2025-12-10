package com.skax.physicalrisk.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 이메일 요청 DTO
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
public class PasswordResetEmailRequest {

	/**
	 * 이메일
	 */
	@NotBlank(message = "이메일은 필수입니다")
	@Email(message = "유효한 이메일 형식이 아닙니다")
	private String email;
}
