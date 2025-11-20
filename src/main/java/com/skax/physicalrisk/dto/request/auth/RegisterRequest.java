package com.skax.physicalrisk.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청")
public class RegisterRequest {

	@Schema(description = "이메일", example = "user@example.com")
	@NotBlank(message = "이메일은 필수입니다")
	@Email(message = "올바른 이메일 형식이 아닙니다")
	private String email;

	@Schema(description = "이름", example = "홍길동")
	@NotBlank(message = "이름은 필수입니다")
	private String name;

	@Schema(description = "비밀번호", example = "password123!")
	@NotBlank(message = "비밀번호는 필수입니다")
	@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
	private String password;

	// @Schema(description = "조직명", example = "SKAX")
	// @NotBlank(message = "조직명은 필수입니다")
	// private String organization;
	// 조직명 삭제

	@Schema(description = "이메일 인증 코드", example = "123456")
	private String verificationCode;
}
