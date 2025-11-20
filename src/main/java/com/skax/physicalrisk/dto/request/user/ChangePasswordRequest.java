package com.skax.physicalrisk.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 변경 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 변경 요청")
public class ChangePasswordRequest {

	@Schema(description = "현재 비밀번호")
	@NotBlank(message = "현재 비밀번호는 필수입니다")
	private String currentPassword;

	@Schema(description = "새 비밀번호", example = "newPassword123!")
	@NotBlank(message = "새 비밀번호는 필수입니다")
	@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
	private String newPassword;
}
