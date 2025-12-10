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
 * 최종 수정일: 2025-11-25
 * 파일 버전: v03 (FastAPI 문서 기준 URL 수정)
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
	 * POST /api/analysis/start
	 *
	 * @param request 분석 요청 DTO
	 * @return 작업 상태 응답
	 */
	public Mono<Map<String, Object>> startAnalysis(StartAnalysisRequestDto request) {
		log.info("FastAPI 분석 시작 요청: siteId={}, hazardTypes={}, priority={}",
			request.getSite().getId(), request.getHazardTypes(), request.getPriority());
		log.debug("전체 요청 본문: {}", request);

		return webClient.post()
			.uri("/api/analysis/start")
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
	 * GET /api/analysis/status?siteId={siteId}&jobId={jobId}
	 *
	 * @param siteId 사업장 ID
	 * @param jobId 작업 ID
	 * @return 작업 상태
	 */
	public Mono<Map<String, Object>> getAnalysisStatus(UUID siteId, UUID jobId) {
		log.info("FastAPI 분석 상태 조회: siteId={}, jobId={}", siteId, jobId);

		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/analysis/status")
				.queryParam("siteId", siteId)
				.queryParam("jobId", jobId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 대시보드 요약 조회 (전체 사업장)
	 *
	 * GET /api/dashboard/summary
	 *
	 * @return 대시보드 요약
	 */
	public Mono<Map<String, Object>> getDashboardSummary() {
		return webClient.get()
			.uri("/api/dashboard/summary")
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 물리적 리스크 점수 조회
	 *
	 * GET /api/analysis/physical-risk-scores?siteId={siteId}&hazardType={hazardType}
	 *
	 * @param siteId 사업장 ID
	 * @param hazardType 위험 유형 (옵션)
	 * @return 물리적 리스크 점수
	 */
	public Mono<Map<String, Object>> getPhysicalRiskScores(UUID siteId, String hazardType) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/analysis/physical-risk-scores")
				.queryParam("siteId", siteId)
				.queryParamIfPresent("hazardType", java.util.Optional.ofNullable(hazardType))
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 과거 재난 이력 조회
	 *
	 * GET /api/analysis/past-events?siteId={siteId}
	 *
	 * @param siteId 사업장 ID
	 * @return 과거 이벤트
	 */
	public Mono<Map<String, Object>> getPastEvents(UUID siteId) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/analysis/past-events")
				.queryParam("siteId", siteId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * SSP 시나리오별 리스크 전망
	 *
	 * GET /api/analysis/ssp?siteId={siteId}&hazardType={hazardType}
	 *
	 * @param siteId 사업장 ID
	 * @param hazardType 위험 유형 (옵션)
	 * @return SSP 전망
	 */
	public Mono<Map<String, Object>> getSSPProjection(UUID siteId, String hazardType) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/analysis/ssp")
				.queryParam("siteId", siteId)
				.queryParamIfPresent("hazardType", java.util.Optional.ofNullable(hazardType))
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 재무 영향 분석
	 *
	 * GET /api/analysis/financial-impacts?siteId={siteId}
	 *
	 * @param siteId 사업장 ID
	 * @return 재무 영향
	 */
	public Mono<Map<String, Object>> getFinancialImpact(UUID siteId) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/analysis/financial-impacts")
				.queryParam("siteId", siteId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 취약성 분석
	 *
	 * GET /api/analysis/vulnerability?siteId={siteId}
	 *
	 * @param siteId 사업장 ID
	 * @return 취약성 분석
	 */
	public Mono<Map<String, Object>> getVulnerability(UUID siteId) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/analysis/vulnerability")
				.queryParam("siteId", siteId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 통합 분석 결과
	 *
	 * GET /api/analysis/total?siteId={siteId}&hazardType={hazardType}
	 *
	 * @param siteId 사업장 ID
	 * @param hazardType 위험 유형
	 * @return 통합 분석 결과
	 */
	public Mono<Map<String, Object>> getTotalAnalysis(UUID siteId, String hazardType) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/analysis/total")
				.queryParam("siteId", siteId)
				.queryParam("hazardType", hazardType)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 사업장 이전 시뮬레이션
	 *
	 * POST /api/simulation/relocation/compare
	 *
	 * @param request 비교 요청
	 * @return 비교 결과
	 */
	public Mono<Map<String, Object>> compareRelocation(Map<String, Object> request) {
		return webClient.post()
			.uri("/api/simulation/relocation/compare")
			.header("X-API-Key", apiKey)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 기후 시뮬레이션
	 *
	 * POST /api/simulation/climate
	 *
	 * @param request 시뮬레이션 요청
	 * @return 시뮬레이션 결과
	 */
	public Mono<Map<String, Object>> runClimateSimulation(Map<String, Object> request) {
		return webClient.post()
			.uri("/api/simulation/climate")
			.header("X-API-Key", apiKey)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 리포트 생성
	 *
	 * POST /api/reports
	 *
	 * @param request 리포트 생성 요청
	 * @return 생성된 리포트 정보
	 */
	public Mono<Map<String, Object>> createReport(Map<String, Object> request) {
		return webClient.post()
			.uri("/api/reports")
			.header("X-API-Key", apiKey)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 리포트 웹 뷰 조회 (리포트 ID 기반)
	 *
	 * GET /api/reports/web?reportId={reportId}
	 *
	 * @param reportId 리포트 ID
	 * @return 웹 뷰 리포트
	 */
	public Mono<Map<String, Object>> getReportWebViewByReportId(String reportId) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/reports/web")
				.queryParam("reportId", reportId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 리포트 웹 뷰 조회 (사용자 ID 기반) - DEPRECATED
	 *
	 * ⚠️ WARNING: FastAPI는 userId가 아닌 reportId를 요구합니다.
	 * 이 메서드는 기존 호환성을 위해 남겨두었지만 작동하지 않을 수 있습니다.
	 *
	 * @param userId 사용자 ID
	 * @return 웹 뷰 리포트
	 * @deprecated FastAPI OpenAPI 스펙과 불일치. getReportWebViewByReportId 사용 권장
	 */
	@Deprecated
	public Mono<Map<String, Object>> getReportWebViewByUserId(UUID userId) {
		log.warn("⚠️ getReportWebViewByUserId는 deprecated됨. FastAPI는 reportId를 요구하지만 userId={}가 전달됨", userId);
		// 임시로 userId를 reportId로 변환 (실제로는 DB에서 매핑 필요)
		return getReportWebViewByReportId(userId.toString());
	}

	/**
	 * 리포트 PDF 다운로드 정보 조회 (리포트 ID 기반)
	 *
	 * GET /api/reports/pdf?reportId={reportId}
	 *
	 * @param reportId 리포트 ID
	 * @return PDF 다운로드 정보
	 */
	public Mono<Map<String, Object>> getReportPdfByReportId(String reportId) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/reports/pdf")
				.queryParam("reportId", reportId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 리포트 PDF 다운로드 정보 조회 (사용자 ID 기반) - DEPRECATED
	 *
	 * ⚠️ WARNING: FastAPI는 userId가 아닌 reportId를 요구합니다.
	 *
	 * @param userId 사용자 ID
	 * @return PDF 다운로드 정보
	 * @deprecated FastAPI OpenAPI 스펙과 불일치. getReportPdfByReportId 사용 권장
	 */
	@Deprecated
	public Mono<Map<String, Object>> getReportPdfByUserId(UUID userId) {
		log.warn("⚠️ getReportPdfByUserId는 deprecated됨. FastAPI는 reportId를 요구하지만 userId={}가 전달됨", userId);
		// 임시로 userId를 reportId로 변환 (실제로는 DB에서 매핑 필요)
		return getReportPdfByReportId(userId.toString());
	}

	/**
	 * 리포트 삭제
	 *
	 * DELETE /api/reports
	 *
	 * @return 삭제 결과
	 */
	public Mono<Map<String, Object>> deleteReport() {
		return webClient.delete()
			.uri("/api/reports")
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 리포트 삭제 (사용자 ID 기반) - DEPRECATED
	 *
	 * ⚠️ WARNING: FastAPI DELETE /api/reports는 파라미터를 받지 않습니다.
	 *
	 * @param userId 사용자 ID (무시됨)
	 * @return 삭제 결과
	 * @deprecated FastAPI OpenAPI 스펙과 불일치. deleteReport() 사용 권장
	 */
	@Deprecated
	public Mono<Map<String, Object>> deleteReportByUserId(UUID userId) {
		log.warn("⚠️ deleteReportByUserId는 deprecated됨. FastAPI는 파라미터 없이 DELETE /api/reports를 호출하지만 userId={}가 전달됨", userId);
		return deleteReport();
	}

	// ============================================================
	// 추가 데이터 관리 API (Additional Data Management)
	// ============================================================

	/**
	 * 추가 데이터 업로드
	 *
	 * POST /api/additional-data
	 *
	 * @param siteId 사업장 ID
	 * @param request 추가 데이터 입력 (siteId 포함)
	 * @return 업로드 응답
	 */
	public Mono<Map<String, Object>> uploadAdditionalData(UUID siteId, Map<String, Object> request) {
		log.info("FastAPI 추가 데이터 업로드: siteId={}", siteId);
		// siteId를 request body에 추가
		request.put("siteId", siteId.toString());
		return webClient.post()
			.uri("/api/additional-data")
			.header("X-API-Key", apiKey)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF)
			.doOnSuccess(response -> log.info("추가 데이터 업로드 성공: {}", response))
			.doOnError(error -> log.error("추가 데이터 업로드 실패", error));
	}

	/**
	 * 추가 데이터 조회 (특정 카테고리)
	 *
	 * GET /api/additional-data?siteId={siteId}&dataCategory={category}
	 *
	 * @param siteId 사업장 ID
	 * @param dataCategory 데이터 카테고리
	 * @return 추가 데이터 응답
	 */
	public Mono<Map<String, Object>> getAdditionalData(UUID siteId, String dataCategory) {
		log.info("FastAPI 추가 데이터 조회: siteId={}, dataCategory={}", siteId, dataCategory);
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/additional-data")
				.queryParam("siteId", siteId)
				.queryParam("dataCategory", dataCategory)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 추가 데이터 삭제
	 *
	 * DELETE /api/additional-data?siteId={siteId}&dataCategory={dataCategory}
	 *
	 * @param siteId 사업장 ID
	 * @param dataCategory 데이터 카테고리
	 * @return 삭제 결과
	 */
	public Mono<Map<String, Object>> deleteAdditionalData(UUID siteId, String dataCategory) {
		log.info("FastAPI 추가 데이터 삭제: siteId={}, dataCategory={}", siteId, dataCategory);
		return webClient.delete()
			.uri(uriBuilder -> uriBuilder
				.path("/api/additional-data")
				.queryParam("siteId", siteId)
				.queryParam("dataCategory", dataCategory)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	// ============================================================
	// 재해 이력 API (Disaster History)
	// ============================================================

	/**
	 * 재해 이력 목록 조회
	 *
	 * GET /api/disaster-history?adminCode={code}&year={year}&disasterType={type}&page={page}&pageSize={size}
	 *
	 * @param adminCode 행정구역 코드 (옵션)
	 * @param year 연도 (옵션)
	 * @param disasterType 재해 유형 (옵션)
	 * @param page 페이지 번호
	 * @param pageSize 페이지당 개수
	 * @return 재해 이력 목록
	 */
	public Mono<Map<String, Object>> getDisasterHistory(
		String adminCode,
		Integer year,
		String disasterType,
		Integer page,
		Integer pageSize
	) {
		log.info("FastAPI 재해 이력 조회: adminCode={}, year={}, disasterType={}, page={}, pageSize={}",
			adminCode, year, disasterType, page, pageSize);
		return webClient.get()
			.uri(uriBuilder -> {
				var builder = uriBuilder.path("/api/disaster-history");
				if (adminCode != null) builder.queryParam("adminCode", adminCode);
				if (year != null) builder.queryParam("year", year);
				if (disasterType != null) builder.queryParam("disasterType", disasterType);
				if (page != null) builder.queryParam("page", page);
				if (pageSize != null) builder.queryParam("pageSize", pageSize);
				return builder.build();
			})
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	// ============================================================
	// 후보지 추천 배치 API (Recommendation Batch)
	// ============================================================

	/**
	 * 후보지 추천 배치 시작
	 *
	 * POST /api/recommendation/batch/start
	 *
	 * @param request 배치 요청
	 * @return 배치 작업 응답
	 */
	public Mono<Map<String, Object>> startRecommendationBatch(Map<String, Object> request) {
		log.info("FastAPI 후보지 추천 배치 시작: jobName={}", request.get("jobName"));
		return webClient.post()
			.uri("/api/recommendation/batch/start")
			.header("X-API-Key", apiKey)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF)
			.doOnSuccess(response -> log.info("배치 작업 시작 성공: {}", response))
			.doOnError(error -> log.error("배치 작업 시작 실패", error));
	}

	/**
	 * 배치 작업 진행 상황 조회
	 *
	 * GET /api/recommendation/batch/progress?batchId={batchId}
	 *
	 * @param batchJobId 배치 작업 ID
	 * @return 진행 상황
	 */
	public Mono<Map<String, Object>> getBatchProgress(UUID batchJobId) {
		log.info("FastAPI 배치 작업 진행 상황 조회: batchId={}", batchJobId);
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/recommendation/batch/progress")
				.queryParam("batchId", batchJobId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}

	/**
	 * 배치 작업 결과 조회
	 *
	 * GET /api/recommendation/batch/result?batchId={batchId}
	 *
	 * @param batchJobId 배치 작업 ID
	 * @return 추천 결과
	 */
	public Mono<Map<String, Object>> getRecommendationResult(UUID batchJobId) {
		log.info("FastAPI 배치 작업 결과 조회: batchId={}", batchJobId);
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/recommendation/batch/result")
				.queryParam("batchId", batchJobId)
				.build())
			.header("X-API-Key", apiKey)
			.retrieve()
			.bodyToMono(MAP_TYPE_REF);
	}
}
