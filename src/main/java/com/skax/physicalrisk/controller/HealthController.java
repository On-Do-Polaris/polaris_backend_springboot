package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 헬스 체크 컨트롤러
 *
 * 최종 수정일: 2025-12-11
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthController {

	@Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8080,https://on-do.site}")
	private String allowedOrigins;

	/**
	 * 헬스 체크
	 *
	 * @return 상태 정보
	 */
	@Operation(
		summary = "헬스 체크",
		description = "API 서버의 상태를 확인합니다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "서버 상태 정보",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				value = "{\"result\": \"success\", \"data\": {\"status\": \"UP\", \"timestamp\": \"2025-12-11T15:30:00\", \"service\": \"Physical Risk Management API\"}}"
			)
		)
	)
	@ApiResponse(
		responseCode = "500",
		description = "서버 내부 오류",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = com.skax.physicalrisk.dto.response.ErrorResponse.class),
			examples = @ExampleObject(
				value = "{\"result\": \"error\", \"message\": \"서버 내부 오류가 발생했습니다.\", \"errorCode\": \"INTERNAL_SERVER_ERROR\", \"timestamp\": \"2025-12-12T16:30:00\"}"
			)
		)
	)
	@GetMapping
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<Map<String, Object>>> health() {
		log.info("Health check requested");

		Map<String, Object> data = new HashMap<>();
		data.put("status", "UP");
		data.put("timestamp", LocalDateTime.now());
		data.put("service", "Physical Risk Management API");

		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success(data));
	}

	/**
	 * CORS 설정 확인 (진단용)
	 * @return 현재 적용된 CORS 설정
	 */
	@Operation(
		summary = "CORS 설정 확인",
		description = "현재 애플리케이션에 적용된 CORS 허용 도메인 목록을 확인합니다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "CORS 설정 정보",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				value = "{\"allowed-origins\": \"http://localhost:3000,http://localhost:5173,https://on-do.site\"}"
			)
		)
	)
	@GetMapping("/cors-check")
	public ResponseEntity<Map<String, String>> checkCors() {
		Map<String, String> corsInfo = new HashMap<>();
		corsInfo.put("allowed-origins", allowedOrigins);
		return ResponseEntity.ok(corsInfo);
	}
}
