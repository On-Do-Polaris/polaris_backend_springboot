package com.skax.physicalrisk.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 인증번호 확인 요청 DTO (2단계)
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 재설정 인증번호 확인 요청")
public class PasswordResetVerifyCodeRequest {

	@Schema(description = "이메일 주소", example = "user@example.com")
	@NotBlank(message = "이메일은 필수입니다")
	@Email(message = "올바른 이메일 형식이 아닙니다")
	private String email;

	@Schema(description = "인증번호", example = "123456")
	@NotBlank(message = "인증번호는 필수입니다")
	private String code;
}
