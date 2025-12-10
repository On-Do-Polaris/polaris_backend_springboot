package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.response.past.PastDisasterResponse;
import com.skax.physicalrisk.service.past.PastDisasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 과거 재해 이력 컨트롤러 (v0.2 신규)
 *
 * 최종 수정일: 2025-12-10
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/past")
@RequiredArgsConstructor
public class PastController {

	private final PastDisasterService pastDisasterService;

	/**
	 * 과거 재해 이력 조회
	 *
	 * GET /api/past?year={year}&disaster_type={disaster_type}&severity={severity}
	 *
	 * @param year         연도 (required)
	 * @param disasterType 재해 유형 (required)
	 * @param severity     심각도 (required)
	 * @return 과거 재해 이력 목록
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 * @throws ValidationException   파라미터가 유효하지 않은 경우 (422)
	 */
	@Operation(
		summary = "과거 재해 이력 조회",
		description = "연도, 재해 유형, 심각도에 따른 과거 재해 이력을 조회한다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "과거 재해 이력 목록",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = PastDisasterResponse.class)
		)
	)
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	@ApiResponse(responseCode = "422", description = "파라미터가 유효하지 않음")
	@GetMapping
	public ResponseEntity<PastDisasterResponse> getPastDisasters(
		@Parameter(description = "연도", required = true, example = "2023")
		@RequestParam int year,
		@Parameter(description = "재해 유형", required = true, example = "호우")
		@RequestParam("disaster_type") String disasterType,
		@Parameter(description = "심각도", required = true, example = "경보")
		@RequestParam String severity
	) {
		log.info("GET /api/past?year={}&disaster_type={}&severity={}", year, disasterType, severity);
		PastDisasterResponse response = pastDisasterService.getPastDisasters(year, disasterType, severity);
		return ResponseEntity.ok(response);
	}
}
