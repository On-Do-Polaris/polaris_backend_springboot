package com.skax.physicalrisk.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

	@NotBlank(message = "이메일은 필수입니다")
	@Email(message = "올바른 이메일 형식이 아닙니다")
	private String email;

	@NotBlank(message = "이름은 필수입니다")
	private String name;

	@NotBlank(message = "비밀번호는 필수입니다")
	@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
	private String password;

	@NotBlank(message = "조직명은 필수입니다")
	private String organization;

	private String verificationCode;
}
