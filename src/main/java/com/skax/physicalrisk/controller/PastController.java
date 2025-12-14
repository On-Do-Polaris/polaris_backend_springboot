package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.response.ErrorResponse;
import com.skax.physicalrisk.dto.response.past.PastDisasterResponse;
import com.skax.physicalrisk.exception.UnauthorizedException;
import com.skax.physicalrisk.service.past.PastDisasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
	 * @param year         연도 (optional)
	 * @param disasterType 재해 유형 (optional)
	 * @param severity     심각도 (optional)
	 * @return 과거 재해 이력 목록
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 * @throws ValidationException   파라미터가 유효하지 않은 경우 (422)
	 */
	@Operation(
		summary = "과거 재해 이력 조회",
		description = "연도, 재해 유형, 심각도에 따른 과거 재해 이력을 조회한다. 모든 파라미터는 선택적이며, 빈 값으로 호출 시 전체 이력을 조회한다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "과거 재해 이력 목록",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = PastDisasterResponse.class),
			examples = @ExampleObject(
				value = "{\"data\":{\"items\":[{\"id\":1,\"date\":\"2023-07-15\",\"disaster_type\":\"호우\",\"severity\":\"경보\",\"region\":[\"서울\",\"경기\"]},{\"id\":2,\"date\":\"2023-08-10\",\"disaster_type\":\"태풍\",\"severity\":\"주의보\",\"region\":[\"부산\",\"경남\"]}]}}"
			)
		)
	)
	@ApiResponse(
		responseCode = "401", 		
		description = "인증되지 않은 사용자",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"인증되지 않은 사용자입니다.\", \"errorCode\": \"UNAUTHORIZED\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@ApiResponse(
		responseCode = "400",
		description = "잘못된 요청",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"잘못된 요청입니다.\", \"errorCode\": \"INVALID_REQUEST\", \"timestamp\": \"2025-12-12T16:30:00\"}")
		)
	)
	@ApiResponse(
		responseCode = "500",
		description = "서버 내부 오류",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"서버 내부 오류가 발생했습니다.\", \"errorCode\": \"INTERNAL_SERVER_ERROR\", \"timestamp\": \"2025-12-12T16:30:00\"}")
		)
	)
	@GetMapping
	public ResponseEntity<PastDisasterResponse> getPastDisasters(
		@Parameter(description = "연도", required = false, example = "2023")
		@RequestParam(required = false) Integer year,
		@Parameter(description = "재해 유형", required = false, example = "호우")
		@RequestParam(value = "disaster_type", required = false) String disasterType,
		@Parameter(description = "심각도", required = false, example = "경보")
		@RequestParam(required = false) String severity
	) {
		log.info("GET /api/past?year={}&disaster_type={}&severity={}", year, disasterType, severity);
		PastDisasterResponse response = pastDisasterService.getPastDisasters(year, disasterType, severity);
		return ResponseEntity.ok(response);
	}
}
