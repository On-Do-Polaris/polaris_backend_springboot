package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.client.fastapi.dto.StartAnalysisRequestDto;
import com.skax.physicalrisk.dto.response.analysis.*;
import com.skax.physicalrisk.service.analysis.AnalysisService;
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
@RequestMapping("/api/sites/{siteId}/analysis")
@RequiredArgsConstructor
public class AnalysisController {

	private final AnalysisService analysisService;

	/**
	 * 분석 시작
	 *
	 * POST /api/sites/{siteId}/analysis/start
	 *
	 * @param siteId  사업장 ID
	 * @param request 분석 요청 (위경도, 유형)
	 * @return 작업 상태
	 */
	@PostMapping("/start")
	public ResponseEntity<AnalysisJobStatusResponse> startAnalysis(
		@PathVariable UUID siteId,
		@RequestBody StartAnalysisRequest request
	) {
		log.info("POST /api/sites/{}/analysis/start", siteId);

		AnalysisJobStatusResponse response = analysisService.startAnalysis(
			siteId,
			request.getLatitude(),
			request.getLongitude(),
			request.getIndustryType()
		);

		log.info("Controller returning success: 200 OK");
		return ResponseEntity.ok(response);
	}

	/**
	 * 분석 작업 상태 조회
	 *
	 * GET /api/sites/{siteId}/analysis/status/{jobId}
	 *
	 * @param siteId 사업장 ID
	 * @param jobId  작업 ID
	 * @return 작업 상태
	 */
	@GetMapping("/status/{jobId}")
	public ResponseEntity<AnalysisJobStatusResponse> getAnalysisStatus(
		@PathVariable UUID siteId,
		@PathVariable UUID jobId
	) {
		log.info("GET /api/sites/{}/analysis/status/{}", siteId, jobId);
		return ResponseEntity.ok(analysisService.getAnalysisStatus(siteId, jobId));
	}

	/**
	 * 대시보드 요약 조회 (전체 사업장)
	 *
	 * GET /api/dashboard/summary
	 *
	 * @return 대시보드 요약
	 */
	@GetMapping("/dashboard/summary")
	public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
		log.info("GET /api/dashboard/summary");
		return ResponseEntity.ok(analysisService.getDashboardSummary());
	}

	/**
	 * 물리적 리스크 점수 조회
	 *
	 * GET /api/sites/{siteId}/analysis/physical-risk-scores
	 *
	 * @param siteId     사업장 ID
	 * @param hazardType 위험 유형 (옵션)
	 * @return 물리적 리스크 점수
	 */
	@GetMapping("/physical-risk-scores")
	public ResponseEntity<PhysicalRiskScoreResponse> getPhysicalRiskScores(
		@PathVariable UUID siteId,
		@RequestParam(required = false) String hazardType
	) {
		log.info("GET /api/sites/{}/analysis/physical-risk-scores?hazardType={}", siteId, hazardType);
		return ResponseEntity.ok(analysisService.getPhysicalRiskScores(siteId, hazardType));
	}

	/**
	 * 과거 재난 이력 조회
	 *
	 * GET /api/sites/{siteId}/analysis/past-events
	 *
	 * @param siteId 사업장 ID
	 * @return 과거 이벤트
	 */
	@GetMapping("/past-events")
	public ResponseEntity<PastEventsResponse> getPastEvents(@PathVariable UUID siteId) {
		log.info("GET /api/sites/{}/analysis/past-events", siteId);
		return ResponseEntity.ok(analysisService.getPastEvents(siteId));
	}

	/**
	 * 재무 영향 분석
	 *
	 * GET /api/sites/{siteId}/analysis/financial-impacts
	 *
	 * @param siteId 사업장 ID
	 * @return 재무 영향
	 */
	@GetMapping("/financial-impacts")
	public ResponseEntity<FinancialImpactResponse> getFinancialImpact(@PathVariable UUID siteId) {
		log.info("GET /api/sites/{}/analysis/financial-impacts", siteId);
		return ResponseEntity.ok(analysisService.getFinancialImpact(siteId));
	}

	/**
	 * 취약성 분석
	 *
	 * GET /api/sites/{siteId}/analysis/vulnerability
	 *
	 * @param siteId 사업장 ID
	 * @return 취약성 분석
	 */
	@GetMapping("/vulnerability")
	public ResponseEntity<VulnerabilityResponse> getVulnerability(@PathVariable UUID siteId) {
		log.info("GET /api/sites/{}/analysis/vulnerability", siteId);
		return ResponseEntity.ok(analysisService.getVulnerability(siteId));
	}

	/**
	 * 통합 분석 결과
	 *
	 * GET /api/sites/{siteId}/analysis/total
	 *
	 * @param siteId     사업장 ID
	 * @param hazardType 위험 유형
	 * @return 통합 분석 결과
	 */
	@GetMapping("/total")
	public ResponseEntity<AnalysisTotalResponse> getTotalAnalysis(
		@PathVariable UUID siteId,
		@RequestParam String hazardType
	) {
		log.info("GET /api/sites/{}/analysis/total?hazardType={}", siteId, hazardType);
		return ResponseEntity.ok(analysisService.getTotalAnalysis(siteId, hazardType));
	}

	/**
	 * 분석 시작 요청 DTO
	 */
	@lombok.Data
	@lombok.NoArgsConstructor
	@lombok.AllArgsConstructor
	public static class StartAnalysisRequest {
		private BigDecimal latitude;
		private BigDecimal longitude;
		private String industryType;
	}
}
