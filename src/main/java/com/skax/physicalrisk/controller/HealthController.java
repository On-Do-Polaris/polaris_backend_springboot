package com.skax.physicalrisk.controller;

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
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

	/**
	 * 헬스 체크
	 *
	 * @return 상태 정보
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> health() {
		log.info("Health check requested");

		Map<String, Object> response = new HashMap<>();
		response.put("status", "UP");
		response.put("timestamp", LocalDateTime.now());
		response.put("service", "Physical Risk Management API");

		return ResponseEntity.ok(response);
	}
}
