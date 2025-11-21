package com.skax.physicalrisk.client.fastapi;

import com.skax.physicalrisk.client.fastapi.dto.SiteInfoDto;
import com.skax.physicalrisk.client.fastapi.dto.StartAnalysisRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * FastAPI 서버 통신 클라이언트
 *
 * AI Agent 분석 요청을 위한 FastAPI 서버 호출
 *
 * 최종 수정일: 2025-11-18
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
@Slf4j
@Component
public class FastApiClient {

	private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE_REF =
		new ParameterizedTypeReference<Map<String, Object>>() {};

	private final WebClient webClient;

	@Value("${fastapi.api-key}")
	private String apiKey;

	public FastApiClient(@Value("${fastapi.base-url}") String baseUrl) {
		this.webClient = WebClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	/**
	 * 분석 시작 요청
	 *
	 * POST /api/v1/analysis/start
	 *
	 * @param request 분석 요청 DTO
	 * @return 작업 상태 응답
	 */
	public Mono<Map<String, Object>> startAnalysis(StartAnalysisRequestDto request) {
		log.info("FastAPI 분석 시작 요청: siteId={}, hazardTypes={}, priority={}",
			request.getSite().getId(), request.getHazardTypes(), request.getPriority());
		log.debug("전체 요청 본문: {}", request);

		return webClient.post()
			.uri("/api/v1/analysis/start")
			.header("X-API-Key", apiKey)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF)
			.doOnSuccess(response -> log.info("분석 시작 성공: {}", response))
			.doOnError(error -> log.error("분석 시작 실패", error));
	}

	/**
	 * 분석 작업 상태 조회
	 *
	 * GET /api/v1/analysis/status/{jobId}
	 *
	 * @param jobId 작업 ID
	 * @return 작업 상태
	 */
	public Mono<Map<String, Object>> getAnalysisStatus(UUID jobId) {
		log.info("FastAPI 분석 상태 조회: jobId={}", jobId);

		return webClient.get()
			.uri("/api/v1/analysis/status/{jobId}", jobId)
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 대시보드 요약 조회 (전체 사업장)
	 *
	 * GET /api/v1/dashboard/summary
	 *
	 * @param userId 사용자 ID
	 * @return 대시보드 요약
	 */
	public Mono<Map<String, Object>> getDashboardSummary(UUID userId) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/v1/dashboard/summary")
				.queryParam("userId", userId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 물리적 리스크 점수 조회
	 *
	 * GET /api/v1/analysis/{siteId}/physical-risk-scores
	 *
	 * @param siteId 사업장 ID
	 * @param hazardType 위험 유형 (옵션)
	 * @return 물리적 리스크 점수
	 */
	public Mono<Map<String, Object>> getPhysicalRiskScores(UUID siteId, String hazardType) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/v1/analysis/{siteId}/physical-risk-scores")
				.queryParamIfPresent("hazardType", java.util.Optional.ofNullable(hazardType))
				.build(siteId))
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 과거 재난 이력 조회
	 *
	 * GET /api/v1/analysis/{siteId}/past-events
	 *
	 * @param siteId 사업장 ID
	 * @return 과거 이벤트
	 */
	public Mono<Map<String, Object>> getPastEvents(UUID siteId) {
		return webClient.get()
			.uri("/api/v1/analysis/{siteId}/past-events", siteId)
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * SSP 시나리오별 리스크 전망
	 *
	 * GET /api/v1/analysis/{siteId}/ssp
	 *
	 * @param siteId 사업장 ID
	 * @param hazardType 위험 유형 (옵션)
	 * @return SSP 전망
	 */
	public Mono<Map<String, Object>> getSSPProjection(UUID siteId, String hazardType) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/v1/analysis/{siteId}/ssp")
				.queryParamIfPresent("hazardType", java.util.Optional.ofNullable(hazardType))
				.build(siteId))
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 재무 영향 분석
	 *
	 * GET /api/v1/analysis/{siteId}/financial-impacts
	 *
	 * @param siteId 사업장 ID
	 * @return 재무 영향
	 */
	public Mono<Map<String, Object>> getFinancialImpact(UUID siteId) {
		return webClient.get()
			.uri("/api/v1/analysis/{siteId}/financial-impacts", siteId)
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 취약성 분석
	 *
	 * GET /api/v1/analysis/{siteId}/vulnerability
	 *
	 * @param siteId 사업장 ID
	 * @return 취약성 분석
	 */
	public Mono<Map<String, Object>> getVulnerability(UUID siteId) {
		return webClient.get()
			.uri("/api/v1/analysis/{siteId}/vulnerability", siteId)
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 통합 분석 결과
	 *
	 * GET /api/v1/analysis/{siteId}/total
	 *
	 * @param siteId 사업장 ID
	 * @param hazardType 위험 유형
	 * @return 통합 분석 결과
	 */
	public Mono<Map<String, Object>> getTotalAnalysis(UUID siteId, String hazardType) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/v1/analysis/{siteId}/total")
				.queryParam("hazardType", hazardType)
				.build(siteId))
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 사업장 이전 시뮬레이션
	 *
	 * POST /api/v1/simulation/relocation/compare
	 *
	 * @param request 비교 요청
	 * @return 비교 결과
	 */
	public Mono<Map<String, Object>> compareRelocation(Map<String, Object> request) {
		return webClient.post()
			.uri("/api/v1/simulation/relocation/compare")
			.header("X-API-Key", apiKey)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 기후 시뮬레이션
	 *
	 * POST /api/v1/simulation/climate
	 *
	 * @param request 시뮬레이션 요청
	 * @return 시뮬레이션 결과
	 */
	public Mono<Map<String, Object>> runClimateSimulation(Map<String, Object> request) {
		return webClient.post()
			.uri("/api/v1/simulation/climate")
			.header("X-API-Key", apiKey)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 리포트 생성
	 *
	 * POST /api/v1/reports
	 *
	 * @param request 리포트 생성 요청
	 * @return 생성된 리포트 정보
	 */
	public Mono<Map<String, Object>> createReport(Map<String, Object> request) {
		return webClient.post()
			.uri("/api/v1/reports")
			.header("X-API-Key", apiKey)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 리포트 웹 뷰 조회 (사용자 ID 기반)
	 *
	 * GET /api/v1/reports/web?userId={userId}
	 *
	 * @param userId 사용자 ID
	 * @return 웹 뷰 리포트
	 */
	public Mono<Map<String, Object>> getReportWebViewByUserId(UUID userId) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/v1/reports/web")
				.queryParam("userId", userId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 리포트 PDF 다운로드 정보 조회 (사용자 ID 기반)
	 *
	 * GET /api/v1/reports/pdf?userId={userId}
	 *
	 * @param userId 사용자 ID
	 * @return PDF 다운로드 정보
	 */
	public Mono<Map<String, Object>> getReportPdfByUserId(UUID userId) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/v1/reports/pdf")
				.queryParam("userId", userId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 리포트 삭제 (사용자 ID 기반)
	 *
	 * DELETE /api/v1/reports?userId={userId}
	 *
	 * @param userId 사용자 ID
	 * @return 삭제 결과
	 */
	public Mono<Map<String, Object>> deleteReportByUserId(UUID userId) {
		return webClient.delete()
			.uri(uriBuilder -> uriBuilder
				.path("/api/v1/reports")
				.queryParam("userId", userId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}
}
