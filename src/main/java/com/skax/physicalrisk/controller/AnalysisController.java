package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.client.fastapi.dto.StartAnalysisRequestDto;
import com.skax.physicalrisk.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
	 * @param request 분석 요청
	 * @return 작업 상태
	 */
	@PostMapping("/start")
	public Mono<ResponseEntity<Map<String, Object>>> startAnalysis(
		@PathVariable UUID siteId,
		@RequestBody StartAnalysisRequest request
	) {
		log.info("POST /api/sites/{}/analysis/start", siteId);

		return analysisService.startAnalysis(
			siteId,
			request.getHazardTypes(),
			request.getPriority(),
			request.getOptions()
		).map(ResponseEntity::ok);
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
	public Mono<ResponseEntity<Map<String, Object>>> getAnalysisStatus(
		@PathVariable UUID siteId,
		@PathVariable UUID jobId
	) {
		log.info("GET /api/sites/{}/analysis/status/{}", siteId, jobId);
		return analysisService.getAnalysisStatus(siteId, jobId)
			.map(ResponseEntity::ok);
	}

	/**
	 * 분석 개요 조회
	 *
	 * GET /api/sites/{siteId}/analysis/overview
	 *
	 * @param siteId 사업장 ID
	 * @return 분석 개요
	 */
	@GetMapping("/overview")
	public Mono<ResponseEntity<Map<String, Object>>> getAnalysisOverview(@PathVariable UUID siteId) {
		log.info("GET /api/sites/{}/analysis/overview", siteId);
		return analysisService.getAnalysisOverview(siteId)
			.map(ResponseEntity::ok);
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
	public Mono<ResponseEntity<Map<String, Object>>> getPhysicalRiskScores(
		@PathVariable UUID siteId,
		@RequestParam(required = false) String hazardType
	) {
		log.info("GET /api/sites/{}/analysis/physical-risk-scores?hazardType={}", siteId, hazardType);
		return analysisService.getPhysicalRiskScores(siteId, hazardType)
			.map(ResponseEntity::ok);
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
	public Mono<ResponseEntity<Map<String, Object>>> getPastEvents(@PathVariable UUID siteId) {
		log.info("GET /api/sites/{}/analysis/past-events", siteId);
		return analysisService.getPastEvents(siteId)
			.map(ResponseEntity::ok);
	}

	/**
	 * SSP 시나리오별 리스크 전망
	 *
	 * GET /api/sites/{siteId}/analysis/ssp
	 *
	 * @param siteId     사업장 ID
	 * @param hazardType 위험 유형 (옵션)
	 * @return SSP 전망
	 */
	@GetMapping("/ssp")
	public Mono<ResponseEntity<Map<String, Object>>> getSSPProjection(
		@PathVariable UUID siteId,
		@RequestParam(required = false) String hazardType
	) {
		log.info("GET /api/sites/{}/analysis/ssp?hazardType={}", siteId, hazardType);
		return analysisService.getSSPProjection(siteId, hazardType)
			.map(ResponseEntity::ok);
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
	public Mono<ResponseEntity<Map<String, Object>>> getFinancialImpact(@PathVariable UUID siteId) {
		log.info("GET /api/sites/{}/analysis/financial-impacts", siteId);
		return analysisService.getFinancialImpact(siteId)
			.map(ResponseEntity::ok);
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
	public Mono<ResponseEntity<Map<String, Object>>> getVulnerability(@PathVariable UUID siteId) {
		log.info("GET /api/sites/{}/analysis/vulnerability", siteId);
		return analysisService.getVulnerability(siteId)
			.map(ResponseEntity::ok);
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
	public Mono<ResponseEntity<Map<String, Object>>> getTotalAnalysis(
		@PathVariable UUID siteId,
		@RequestParam String hazardType
	) {
		log.info("GET /api/sites/{}/analysis/total?hazardType={}", siteId, hazardType);
		return analysisService.getTotalAnalysis(siteId, hazardType)
			.map(ResponseEntity::ok);
	}

	/**
	 * 분석 시작 요청 DTO
	 */
	@lombok.Data
	@lombok.NoArgsConstructor
	@lombok.AllArgsConstructor
	public static class StartAnalysisRequest {
		private List<String> hazardTypes;
		private String priority;
		private StartAnalysisRequestDto.AnalysisOptions options;
	}
}
