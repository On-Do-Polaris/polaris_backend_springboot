package com.skax.physicalrisk.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 정보")
public class UserResponse {

	@Schema(description = "이메일", example = "user@example.com")
	private String email;

	@Schema(description = "이름", example = "홍길동")
	private String name;

	@Schema(description = "언어 설정", example = "ko", allowableValues = {"ko", "en"})
	private String language;
}
