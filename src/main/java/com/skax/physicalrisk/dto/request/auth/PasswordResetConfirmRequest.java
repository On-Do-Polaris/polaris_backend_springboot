package com.skax.physicalrisk.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 확인 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 재설정 확인 요청")
public class PasswordResetConfirmRequest {

	@Schema(description = "재설정 토큰")
	@NotBlank(message = "토큰은 필수입니다")
	private String token;

	@Schema(description = "새 비밀번호", example = "newPassword123!")
	@NotBlank(message = "새 비밀번호는 필수입니다")
	@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
	private String newPassword;
}
