package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.response.ErrorResponse;
import com.skax.physicalrisk.dto.response.analysis.DashboardSummaryResponse;
import com.skax.physicalrisk.service.analysis.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 컨트롤러
 *
 * 최종 수정일: 2025-11-20
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final AnalysisService analysisService;

	/**
	 * 대시보드 정보 조회
	 *
	 * @return 대시보드 정보 (mainClimateRisk: 주요 기후 리스크, sites: 사업장 목록 및 리스크 점수)
	 */
	@Operation(
		summary = "대시보드 정보 조회",
		description = "메인 대시보드에서 보여줄 정보를 조회합니다. 주요 기후 리스크와 각 사업장의 리스크 점수를 포함합니다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "대시보드 정보 반환",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = DashboardSummaryResponse.class),
			examples = @ExampleObject(
				value = "{\"mainClimateRisk\": \"극심한 고온\", \"sites\": [{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"siteName\": \"sk u 타워\", \"latitude\": 37.36633726, \"longitude\": 127.10661717, \"jibunAddress\": \"경기도 성남시 분당구 정자동 25-1\", \"roadAddress\": \"경기도 성남시 분당구 성남대로343번길 9\", \"siteType\": \"data_center\", \"totalRiskScore\": 75}]}"
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
	@GetMapping
	public ResponseEntity<DashboardSummaryResponse> getDashboard() {
		log.info("GET /api/dashboard - Fetching dashboard info");
		DashboardSummaryResponse response = analysisService.getDashboardSummary();
		return ResponseEntity.ok(response);
	}
}
