package com.skax.physicalrisk.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
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
	@GetMapping
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<Map<String, Object>>> health() {
		log.info("Health check requested");

		Map<String, Object> data = new HashMap<>();
		data.put("status", "UP");
		data.put("timestamp", LocalDateTime.now());
		data.put("service", "Physical Risk Management API");

		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success(data));
	}
}
