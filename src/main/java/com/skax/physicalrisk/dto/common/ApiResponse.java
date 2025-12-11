package com.skax.physicalrisk.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 표준 API 응답 래퍼
 *
 * 모든 API 응답은 이 구조를 따릅니다:
 * - result: "success" 또는 "error"
 * - message: 응답 메시지 (선택적)
 * - data: 실제 응답 데이터 (선택적)
 *
 * 최종 수정일: 2025-12-11
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "표준 API 응답")
public class ApiResponse<T> {

	@Schema(description = "응답 결과", example = "success", allowableValues = {"success", "error"})
	private String result;

	@Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
	private String message;

	@Schema(description = "응답 데이터")
	private T data;

	/**
	 * 성공 응답 (데이터 없음)
	 */
	public static <T> ApiResponse<T> success() {
		return ApiResponse.<T>builder()
			.result("success")
			.build();
	}

	/**
	 * 성공 응답 (메시지 포함)
	 */
	public static <T> ApiResponse<T> success(String message) {
		return ApiResponse.<T>builder()
			.result("success")
			.message(message)
			.build();
	}

	/**
	 * 성공 응답 (데이터 포함)
	 */
	public static <T> ApiResponse<T> success(T data) {
		return ApiResponse.<T>builder()
			.result("success")
			.data(data)
			.build();
	}

	/**
	 * 성공 응답 (메시지 + 데이터 포함)
	 */
	public static <T> ApiResponse<T> success(String message, T data) {
		return ApiResponse.<T>builder()
			.result("success")
			.message(message)
			.data(data)
			.build();
	}

	/**
	 * 에러 응답 (메시지만)
	 */
	public static <T> ApiResponse<T> error(String message) {
		return ApiResponse.<T>builder()
			.result("error")
			.message(message)
			.build();
	}

	/**
	 * 에러 응답 (메시지 + 데이터)
	 */
	public static <T> ApiResponse<T> error(String message, T data) {
		return ApiResponse.<T>builder()
			.result("error")
			.message(message)
			.data(data)
			.build();
	}
}
