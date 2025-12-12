package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.client.fastapi.dto.StartAnalysisRequestDto;
import com.skax.physicalrisk.dto.response.ErrorResponse;
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
			examples = @ExampleObject(
				value = "{\"result\": \"success\", \"message\": \"분석이 시작되었습니다.\"}"
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
		responseCode = "404",
		description = "사업장을 찾을 수 없음",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"사업장을 찾을 수 없습니다.\", \"errorCode\": \"SITE_NOT_FOUND\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@PostMapping("/start")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<Void>> startAnalysis(
		@RequestBody StartAnalysisRequest request
	) {
		log.info("POST /api/analysis/start - sites: {}", request.getSites());

		// 모든 사업장 분석 시작
		analysisService.startAnalysisMultiple(request.getSites());

		log.info("Analysis started successfully for all sites");
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success("분석이 시작되었습니다."));
	}

	/**
	 * 분석 작업 상태 조회 (v0.2: jobId 제거, status만 반환)
	 *
	 * GET /api/analysis/status
	 *
	 * @param jobid 작업 ID (사용하지 않음, 호환성을 위해 유지)
	 * @return 작업 상태 (ing: 분석 중, done: 분석 완료)
	 */
	@Operation(
		summary = "분석 상태 확인",
		description = "사용자의 분석 진행 상태를 확인하는 API. status 값이 'ing'면 분석 중, 'done'이면 분석 완료."
	)
	@ApiResponse(
		responseCode = "200",
		description = "분석 중 (status: ing)",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = com.skax.physicalrisk.dto.common.ApiResponse.class),
			examples = {
				@ExampleObject(
					name = "분석 중",
					description = "분석이 진행 중인 경우",
					value = "{\"result\": \"success\", \"data\": {\"status\": \"ing\"}}"
				),
				@ExampleObject(
					name = "분석 완료",
					description = "분석이 완료된 경우",
					value = "{\"result\": \"success\", \"data\": {\"status\": \"done\"}}"
				)
			}
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
		responseCode = "500",
		description = "서버 내부 오류",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"서버 내부 오류가 발생했습니다.\", \"errorCode\": \"INTERNAL_SERVER_ERROR\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@ApiResponse(
		responseCode = "503",
		description = "FastAPI 서버 연결 실패",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"FastAPI 서버 연결에 실패했습니다.\", \"errorCode\": \"FASTAPI_CONNECTION_ERROR\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@GetMapping("/status")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<AnalysisJobStatusResponse>> getAnalysisStatus(
		@Parameter(description = "작업 ID (사용하지 않음, 호환성을 위해 유지)", required = false)
		@RequestParam(required = false) UUID jobid
	) {
		log.info("GET /api/analysis/status");
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success(analysisService.getAnalysisStatus(jobid)));
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
			examples = @ExampleObject(
				value = "{\"result\": \"success\", \"data\": {\"mainClimateRisk\": \"태풍\", \"mainClimateRiskScore\": 70, \"mainClimateRiskAAL\": 17, \"physical-risk-scores\": {\"extreme_heat\": 10, \"extreme_cold\": 20, \"river_flood\": 30, \"urban_flood\": 40, \"drought\": 50, \"water_stress\": 60, \"sea_level_rise\": 50, \"typhoon\": 70, \"wildfire\": 60}, \"aal-scores\": {\"extreme_heat\": 9, \"extreme_cold\": 10, \"river_flood\": 11, \"urban_flood\": 12, \"drought\": 13, \"water_stress\": 14, \"sea_level_rise\": 15, \"typhoon\": 17, \"wildfire\": 16}}}"
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
		responseCode = "404",
		description = "사업장을 찾을 수 없음",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"사업장을 찾을 수 없습니다.\", \"errorCode\": \"SITE_NOT_FOUND\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@GetMapping("/summary")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<java.util.Map<String, Object>>> getAnalysisSummary(
		@Parameter(description = "사업장 ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		@RequestParam UUID siteId
	) {
		log.info("GET /api/analysis/summary?siteId={}", siteId);
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success(analysisService.getAnalysisSummary(siteId)));
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
			examples = {
				@ExampleObject(
					name = "장기",
					description = "장기 리스크 값",
					value = "{\"result\": \"success\", \"data\": {\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"term\": \"long\", \"hazardType\": \"극심한 고온\", \"scenarios1\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios2\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios3\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios4\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"Strategy\": \"냉각 시스템 강화 및 단열재 보강\"}}"
				),
				@ExampleObject(
					name = "중기",
					description = "중기 리스크 값",
					value = "{\"result\": \"success\", \"data\": {\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"term\": \"mid\", \"hazardType\": \"극심한 고온\", \"scenarios1\": {\"point1\": 65, \"point2\": 70, \"point3\": 75, \"point4\": 80, \"point5\": 85}, \"scenarios2\": {\"point1\": 66, \"point2\": 71, \"point3\": 76, \"point4\": 81, \"point5\": 86}, \"scenarios3\": {\"point1\": 67, \"point2\": 72, \"point3\": 77, \"point4\": 82, \"point5\": 87}, \"scenarios4\": {\"point1\": 68, \"point2\": 73, \"point3\": 78, \"point4\": 83, \"point5\": 88}, \"Strategy\": \"재생 에너지 사용 확대 및 에너지 효율 개선\"}}"
				),
				@ExampleObject(
					name = "단기",
					description = "단기 리스크 값",
					value = "{\"result\": \"success\", \"data\": {\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"term\": \"short\", \"hazardType\": \"극심한 고온\", \"scenarios1\": {\"point1\": 50}, \"scenarios2\": {\"point1\": 52}, \"scenarios3\": {\"point1\": 54}, \"scenarios4\": {\"point1\": 56}, \"Strategy\": \"기후 변화 교육 및 인식 제고 프로그램 실시\"}}"
				)
			}
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
		responseCode = "404",
		description = "사업장을 찾을 수 없음",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"사업장을 찾을 수 없습니다.\", \"errorCode\": \"SITE_NOT_FOUND\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@GetMapping("/physical-risk")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<PhysicalRiskScoreResponse>> getPhysicalRisk(
		@Parameter(description = "사업장 ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		@RequestParam UUID siteId,
		@Parameter(description = "기간", required = true, example = "long")
		@RequestParam String term,
		@Parameter(description = "위험 유형", required = true, example = "극심한 고온")
		@RequestParam String hazardType
	) {
		log.info("GET /api/analysis/physical-risk?siteId={}&term={}&hazardType={}", siteId, term, hazardType);
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success(analysisService.getPhysicalRiskScores(siteId, hazardType)));
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
			examples = {
				@ExampleObject(
					name = "장기",
					description = "장기 AAL 값",
					value = "{\"result\": \"success\", \"data\": {\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"term\": \"long\", \"hazardType\": \"극심한 고온\", \"scenarios1\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios2\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios3\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"scenarios4\": {\"point1\": 72, \"point2\": 78, \"point3\": 84, \"point4\": 89}, \"reason\": \"태풍으로 인한 시설 피해 복구 비용, 생산 중단에 따른 매출 손실\"}}"
				),
				@ExampleObject(
					name = "중기",
					description = "중기 AAL 값",
					value = "{\"result\": \"success\", \"data\": {\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"term\": \"mid\", \"hazardType\": \"극심한 고온\", \"scenarios1\": {\"point1\": 65, \"point2\": 70, \"point3\": 75, \"point4\": 80, \"point5\": 85}, \"scenarios2\": {\"point1\": 66, \"point2\": 71, \"point3\": 76, \"point4\": 81, \"point5\": 86}, \"scenarios3\": {\"point1\": 67, \"point2\": 72, \"point3\": 77, \"point4\": 82, \"point5\": 87}, \"scenarios4\": {\"point1\": 68, \"point2\": 73, \"point3\": 78, \"point4\": 83, \"point5\": 88}, \"reason\": \"태풍으로 인한 시설 피해 복구 비용, 생산 중단에 따른 매출 손실\"}}"
				),
				@ExampleObject(
					name = "단기",
					description = "단기 AAL 값",
					value = "{\"result\": \"success\", \"data\": {\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"term\": \"short\", \"hazardType\": \"극심한 고온\", \"scenarios1\": {\"point1\": 50}, \"scenarios2\": {\"point1\": 52}, \"scenarios3\": {\"point1\": 54}, \"scenarios4\": {\"point1\": 56}, \"reason\": \"태풍으로 인한 시설 피해 복구 비용, 생산 중단에 따른 매출 손실\"}}"
				)
			}
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
		responseCode = "404",
		description = "사업장을 찾을 수 없음",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"사업장을 찾을 수 없습니다.\", \"errorCode\": \"SITE_NOT_FOUND\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@GetMapping("/aal")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<FinancialImpactResponse>> getAal(
		@Parameter(description = "사업장 ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		@RequestParam UUID siteId,
		@Parameter(description = "기간", required = true, example = "long")
		@RequestParam String term,
		@Parameter(description = "위험 유형", required = true, example = "극심한 고온")
		@RequestParam String hazardType
	) {
		log.info("GET /api/analysis/aal?siteId={}&term={}&hazardType={}", siteId, term, hazardType);
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success(analysisService.getFinancialImpact(siteId)));
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
			examples = @ExampleObject(
				value = "{\"result\": \"success\", \"data\": {\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"siteName\": \"sk u 타워\", \"latitude\": 37.36633726, \"longitude\": 127.10661717, \"jibunAddress\": \"경기도 성남시 분당구 정자동 25-1\", \"roadAddress\": \"경기도 성남시 분당구 성남대로343번길 9\", \"siteType\": \"data_center\", \"useAprDay\": \"2020-01-01\", \"area\": 1228.5, \"grndflrCnt\": 5, \"ugrnFlrCnt\": 1, \"rserthqkDsgnApplyYn\": \"Y\", \"aisummry\": \"해당 건물은 내진 설계가 되어 있어 지진에 대한 리스크가 없습니다.\"}}"
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
		responseCode = "404",
		description = "사업장을 찾을 수 없음",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"사업장을 찾을 수 없습니다.\", \"errorCode\": \"SITE_NOT_FOUND\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@GetMapping("/vulnerability")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<VulnerabilityResponse>> getVulnerability(
		@Parameter(description = "사업장 ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		@RequestParam UUID siteId
	) {
		log.info("GET /api/analysis/vulnerability?siteId={}", siteId);
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success(analysisService.getVulnerability(siteId)));
	}

	/**
	 * 분석 완료 콜백 (FastAPI → Java)
	 *
	 * POST /api/analysis/complete
	 *
	 * @param request 완료 알림 요청 (userId)
	 * @return 성공 응답
	 */
	@Operation(
		summary = "분석 완료 콜백",
		description = "FastAPI에서 분석 완료 시 호출하는 콜백 엔드포인트. 사용자에게 완료 이메일을 발송합니다.",
		hidden = true
	)
	@ApiResponse(
		responseCode = "200",
		description = "알림 발송 성공",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				value = "{\"result\": \"success\", \"message\": \"분석 완료 알림이 발송되었습니다.\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "404",
		description = "사용자를 찾을 수 없음",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"사용자를 찾을 수 없습니다.\", \"errorCode\": \"USER_NOT_FOUND\", \"timestamp\": \"2025-12-12T15:30:00\"}")
		)
	)
	@PostMapping("/complete")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<Void>> notifyAnalysisCompletion(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "사용자 ID",
			required = true,
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = AnalysisCompleteRequest.class),
				examples = @ExampleObject(
					value = "{\"userId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"}"
				)
			)
		)
		@RequestBody AnalysisCompleteRequest request
	) {
		log.info("POST /api/analysis/complete - userId: {}", request.getUserId());
		analysisService.notifyAnalysisCompletion(request.getUserId());
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success("분석 완료 알림이 발송되었습니다."));
	}

	/**
	 * 분석 완료 요청 DTO
	 */
	@lombok.Data
	@lombok.NoArgsConstructor
	@lombok.AllArgsConstructor
	@Schema(description = "분석 완료 알림 요청")
	public static class AnalysisCompleteRequest {
		@Schema(description = "사용자 ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		private UUID userId;
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
