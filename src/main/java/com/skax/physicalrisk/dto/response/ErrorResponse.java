package com.skax.physicalrisk.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 *
 * @author SKAX Team
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "에러 응답")
public class ErrorResponse {

	@Schema(description = "에러 코드", example = "USER_NOT_FOUND")
	private String code;

	@Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다")
	private String message;

	@Schema(description = "발생 시간")
	@Builder.Default
	private LocalDateTime timestamp = LocalDateTime.now();
}
