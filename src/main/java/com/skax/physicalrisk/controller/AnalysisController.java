package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.client.fastapi.dto.StartAnalysisRequestDto;
import com.skax.physicalrisk.dto.response.analysis.*;
import com.skax.physicalrisk.service.analysis.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * 분석 컨트롤러
 *
 * FastAPI AI Agent를 통한 물리적 리스크 분석
 *
 * 최종 수정일: 2025-11-18
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

	private final AnalysisService analysisService;

	/**
	 * 분석 시작 (v0.2 - jobId 제거, 단순 성공 응답)
	 *
	 * POST /api/analysis/start
	 *
	 * @param request 분석 요청 (sites: 사업장 ID 배열)
	 * @return 성공 메시지 {"result": "success"}
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 * @throws ResourceNotFoundException 사업장을 찾을 수 없는 경우 (404)
	 */
	@Operation(
		summary = "분석 시작",
		description = "회원가입 후 첫 로그인 시 사업장 등록 후 분석을 위해 호출할 API.\n사업장 등록5 화면에서 완료 버튼 누를 때 호출되며,\n모든 사업장을 대상으로 통합 분석을 수행하고 분석 시행 결과를 반환한다.\n모든 사업장의 분석 결과가 다 나올 때까지 로딩을 띄울 것이며 해당 엔드포인트 반환 값은 무시할 예정."
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "분석할 사업장 목록",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = StartAnalysisRequest.class),
			examples = @ExampleObject(
				value = "{\"sites\": [{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"}, {\"siteId\": \"3fa96f64-5789-6859-b3fc-2c963f23dhi6\"}]}"
			)
		)
	)
	@ApiResponse(
		responseCode = "200",
		description = "분석 시작 결과",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = java.util.Map.class),
			examples = @ExampleObject(
				value = "{\"result\": \"success\"}"
			)
		)
	)
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	@ApiResponse(responseCode = "404", description = "사업장을 찾을 수 없음")
	@PostMapping("/start")
	public ResponseEntity<java.util.Map<String, String>> startAnalysis(
		@RequestBody StartAnalysisRequest request
	) {
		log.info("POST /api/analysis/start - sites: {}", request.getSites());

		// 모든 사업장 분석 시작
		analysisService.startAnalysisMultiple(request.getSites());

		log.info("Analysis started successfully for all sites");
		return ResponseEntity.ok(java.util.Map.of("result", "success"));
	}

	/**
	 * 분석 작업 상태 조회
	 *
	 * GET /api/analysis/status?siteId={siteId}&jobId={jobId}
	 *
	 * @param siteId 사업장 ID
	 * @param jobId  작업 ID
	 * @return 작업 상태
	 */
	@Operation(
		summary = "분석 상태 확인",
		description = "회원가입 후 첫 로그인 시 사업장 등록 후 수행된 분석의 진행 상태를 확인하는 API.\n모든 사업장의 분석 결과가 다 나올 때까지 로딩을 띄울 건데 분석 결과가 나왔는지 확인하는 api.\n입력은 없거나 jobid."
	)
	@ApiResponse(
		responseCode = "200",
		description = "현재 분석 상태 반환. 현재 상태를 알려주는 값이 있어야 함.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = AnalysisJobStatusResponse.class),
			examples = @ExampleObject(
				value = "{\"status\": \"ing\"}"
			)
		)
	)
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	@GetMapping("/status")
	public ResponseEntity<AnalysisJobStatusResponse> getAnalysisStatus(
		@Parameter(description = "사업장 ID (선택)", required = false)
		@RequestParam(required = false) UUID siteId,
		@Parameter(description = "통합 또는 개별 분석 jobId (선택)", required = false)
		@RequestParam(required = false) UUID jobId
	) {
		log.info("GET /api/analysis/status?siteId={}&jobId={}", siteId, jobId);
		return ResponseEntity.ok(analysisService.getAnalysisStatus(siteId, jobId));
	}

	/**
	 * 분석 개요
	 *
	 * GET /api/analysis/summary?siteId={siteId}
	 *
	 * @param siteId 사업장 ID
	 * @return 분석 개요 정보
	 */
	@Operation(
		summary = "분석 개요",
		description = "분석 개요 페이지에 보여줄 값 필요.\n보여주는 값은 ssp2-2024 값."
	)
	@ApiResponse(
		responseCode = "200",
		description = "분석 개요 정보",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = java.util.Map.class),
			examples = @ExampleObject(
				value = "{\"mainClimateRisk\": \"태풍\", \"mainClimateRiskScore\": 70, \"mainClimateRiskAAL\": 17, \"physical-risk-scores\": [{\"extreme_heat\": 10, \"extreme_cold\": 20, \"river_flood\": 30, \"urban_flood\": 40, \"drought\": 50, \"water_stress\": 60, \"sea_level_rise\": 50, \"typhoon\": 70, \"wildfire\": 60}], \"aal-scores\": [{\"extreme_heat\": 9, \"extreme_cold\": 10, \"river_flood\": 11, \"urban_flood\": 12, \"drought\": 13, \"water_stress\": 14, \"sea_level_rise\": 15, \"typhoon\": 17, \"wildfire\": 16}]}"
			)
		)
	)
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	@ApiResponse(responseCode = "404", description = "사업장을 찾을 수 없음")
	@GetMapping("/summary")
	public ResponseEntity<java.util.Map<String, Object>> getAnalysisSummary(
		@Parameter(description = "사업장 ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		@RequestParam UUID siteId
	) {
		log.info("GET /api/analysis/summary?siteId={}", siteId);
		// TODO: 서비스 메서드 구현 필요
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}

	/**
	 * 물리적 리스크 값
	 *
	 * GET /api/analysis/physical-risk?siteId={siteId}&term={term}&hazardType={hazardType}
	 *
	 * @param siteId     사업장 ID
	 * @param term       기간 (long, mid, short)
	 * @param hazardType 위험 유형
	 * @return 물리적 리스크 시나리오별 값
	 */
	@Operation(
		summary = "물리적 리스크 값",
		description = "물리적 리스크 값(현 ssp 탭).\n사업장, 기간(term), 리스크 종류(hazardType)에 따라 시나리오-연도 값 반환.\n단 시나리오는 변하지 않으니, 시나리오를 키값으로 지정.\n중기 그래프의 경우 point5까지 포함될 수 있다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "물리적 리스크 시나리오별 값",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = java.util.Map.class),
			examples = @ExampleObject(
				value = "{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"term\": \"long\", \"hazardType\": \"극심한 고온\", \"scenarios1\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios2\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios3\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios4\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"Strategy\": \"냉각 시스템 강화 및 단열재 보강\"}"
			)
		)
	)
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	@ApiResponse(responseCode = "404", description = "사업장을 찾을 수 없음")
	@GetMapping("/physical-risk")
	public ResponseEntity<java.util.Map<String, Object>> getPhysicalRisk(
		@Parameter(description = "사업장 ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		@RequestParam UUID siteId,
		@Parameter(description = "기간", required = true, example = "long")
		@RequestParam String term,
		@Parameter(description = "위험 유형", required = true, example = "극심한 고온")
		@RequestParam String hazardType
	) {
		log.info("GET /api/analysis/physical-risk?siteId={}&term={}&hazardType={}", siteId, term, hazardType);
		// TODO: 서비스 메서드 구현 필요
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}

	/**
	 * AAL 리스크 값
	 *
	 * GET /api/analysis/aal?siteId={siteId}&term={term}&hazardType={hazardType}
	 *
	 * @param siteId     사업장 ID
	 * @param term       기간 (long, mid, short)
	 * @param hazardType 위험 유형
	 * @return AAL 시나리오별 값
	 */
	@Operation(
		summary = "AAL 리스크 값",
		description = "AAL 리스크 값(현 재무 영향 탭).\n사업장, 기간(term), 리스크 종류(hazardType)에 따라 시나리오-연도 값 반환.\n단 시나리오는 변하지 않으니, 시나리오를 키값으로 지정.\n중기 그래프의 경우 point5까지 포함될 수 있다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "AAL 시나리오별 값",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = java.util.Map.class),
			examples = @ExampleObject(
				value = "{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"term\": \"long\", \"hazardType\": \"극심한 고온\", \"scenarios1\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios2\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios3\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios4\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"reason\": \"태풍으로 인한 시설 피해 복구 비용, 생산 중단에 따른 매출 손실\"}"
			)
		)
	)
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	@ApiResponse(responseCode = "404", description = "사업장을 찾을 수 없음")
	@GetMapping("/aal")
	public ResponseEntity<java.util.Map<String, Object>> getAal(
		@Parameter(description = "사업장 ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		@RequestParam UUID siteId,
		@Parameter(description = "기간", required = true, example = "long")
		@RequestParam String term,
		@Parameter(description = "위험 유형", required = true, example = "극심한 고온")
		@RequestParam String hazardType
	) {
		log.info("GET /api/analysis/aal?siteId={}&term={}&hazardType={}", siteId, term, hazardType);
		// TODO: 서비스 메서드 구현 필요
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}

	/**
	 * 취약성 분석
	 *
	 * GET /api/analysis/vulnerability?siteId={siteId}
	 *
	 * @param siteId 사업장 ID
	 * @return 취약성 분석
	 */
	@Operation(
		summary = "분석 취약성 탭",
		description = "특정 사업장에 대한 취약성 및 기본 정보를 반환한다.\n기존 siteInfo 객체를 풀어서 최상위 필드로 노출하고, AI 요약을 함께 제공한다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "취약성 및 사업장 정보",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = VulnerabilityResponse.class),
			examples = @ExampleObject(
				value = "{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"siteName\": \"sk u 타워\", \"latitude\": 37.36633726, \"longitude\": 127.10661717, \"jibunAddress\": \"경기도 성남시 분당구 정자동 25-1\", \"roadAddress\": \"경기도 성남시 분당구 성남대로343번길 9\", \"siteType\": \"data_center\", \"useAprDay\": \"2020-01-01\", \"area\": 1228.5, \"grndflrCnt\": 5, \"ugrnFlrCnt\": 1, \"rserthqkDsgnApplyYn\": \"Y\", \"aisummry\": \"해당 건물은 내진 설계가 되어 있어 지진에 대한 리스크가 없습니다.\"}"
			)
		)
	)
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	@ApiResponse(responseCode = "404", description = "사업장을 찾을 수 없음")
	@GetMapping("/vulnerability")
	public ResponseEntity<VulnerabilityResponse> getVulnerability(
		@Parameter(description = "사업장 ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		@RequestParam UUID siteId
	) {
		log.info("GET /api/analysis/vulnerability?siteId={}", siteId);
		return ResponseEntity.ok(analysisService.getVulnerability(siteId));
	}


	/**
	 * 분석 시작 요청 DTO (v0.2)
	 */
	@lombok.Data
	@lombok.NoArgsConstructor
	@lombok.AllArgsConstructor
	public static class StartAnalysisRequest {
		private List<SiteIdWrapper> sites;

		@lombok.Data
		@lombok.NoArgsConstructor
		@lombok.AllArgsConstructor
		public static class SiteIdWrapper {
			private UUID siteId;
		}
	}
}
