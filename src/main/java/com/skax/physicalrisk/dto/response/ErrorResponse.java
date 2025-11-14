package com.skax.physicalrisk.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

	private String code; // 에러 코드

	private String message; // 에러 메시지

	@Builder.Default
	private LocalDateTime timestamp = LocalDateTime.now(); // 발생 시간
}
