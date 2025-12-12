package com.skax.physicalrisk.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 완료 요청 DTO (3단계)
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 재설정 완료 요청")
public class PasswordResetCompleteRequest {

	@Schema(description = "이메일 주소", example = "user@example.com")
	@NotBlank(message = "이메일은 필수입니다")
	@Email(message = "올바른 이메일 형식이 아닙니다")
	private String email;

	@Schema(description = "새 비밀번호", example = "newPassword123!")
	@NotBlank(message = "새 비밀번호는 필수입니다")
	@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
	private String newPassword;
}
