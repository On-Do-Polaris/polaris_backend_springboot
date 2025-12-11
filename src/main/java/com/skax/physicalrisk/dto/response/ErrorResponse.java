package com.skax.physicalrisk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 에러 응답 DTO
 *
 * 표준 에러 응답 구조:
 * - result: "error" (고정값)
 * - message: 에러 메시지
 * - errorCode: 에러 코드 (code 필드와 동일, 하위 호환성)
 * - timestamp: 에러 발생 시각
 *
 * 최종 수정일: 2025-12-11
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "에러 응답")
public class ErrorResponse {

	@Schema(description = "응답 결과 (항상 error)", example = "error")
	@Builder.Default
	private String result = "error";

	@Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다")
	private String message;

	@Schema(description = "에러 코드", example = "USER_NOT_FOUND")
	private String errorCode;

	@Schema(description = "에러 코드 (하위 호환성)", example = "USER_NOT_FOUND")
	private String code;  // 하위 호환성을 위해 유지

	@Schema(description = "발생 시간", example = "2025-12-11T15:30:00")
	@Builder.Default
	private LocalDateTime timestamp = LocalDateTime.now();

	@Schema(description = "추가 상세 정보")
	private Map<String, Object> details;

	/**
	 * 기본 에러 응답 생성
	 */
	public static ErrorResponse of(String message, String errorCode) {
		return ErrorResponse.builder()
			.result("error")
			.message(message)
			.errorCode(errorCode)
			.code(errorCode)  // 하위 호환성
			.build();
	}
}
