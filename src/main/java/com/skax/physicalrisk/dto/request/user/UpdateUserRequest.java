package com.skax.physicalrisk.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 수정 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 수정 요청")
public class UpdateUserRequest {

	@Schema(description = "언어 설정", example = "ko", allowableValues = {"ko", "en"})
	private String language;
}
