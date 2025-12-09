package com.skax.physicalrisk.service.recommendation;

import com.skax.physicalrisk.client.fastapi.FastApiClient;
import com.skax.physicalrisk.domain.batch.entity.BatchStatus;
import com.skax.physicalrisk.domain.batch.entity.JobType;
import com.skax.physicalrisk.domain.site.entity.Site;
import com.skax.physicalrisk.domain.site.repository.SiteRepository;
import com.skax.physicalrisk.dto.request.recommendation.CandidateSite;
import com.skax.physicalrisk.dto.request.recommendation.SiteRecommendationBatchRequest;
import com.skax.physicalrisk.dto.response.recommendation.BatchProgressResponse;
import com.skax.physicalrisk.dto.response.recommendation.RecommendationItem;
import com.skax.physicalrisk.dto.response.recommendation.SiteRecommendationBatchResponse;
import com.skax.physicalrisk.dto.response.recommendation.SiteRecommendationResultResponse;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import com.skax.physicalrisk.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 후보지 추천 서비스
 *
 * FastAPI 후보지 추천 배치 API 프록시
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

	private final FastApiClient fastApiClient;
	private final SiteRepository siteRepository;

	/**
	 * 후보지 추천 배치 시작
	 *
	 * @param request 배치 요청
	 * @return 배치 작업 응답
	 */
	public Mono<SiteRecommendationBatchResponse> startRecommendationBatch(SiteRecommendationBatchRequest request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("후보지 추천 배치 시작: userId={}, jobName={}, candidatesCount={}",
			userId, request.getJobName(), request.getCandidates().size());

		// 기준 사업장이 지정된 경우 소유권 검증
		if (request.getReferenceSiteId() != null) {
			Site referenceSite = siteRepository.findById(request.getReferenceSiteId())
				.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND));

			if (!referenceSite.getUser().getId().equals(userId)) {
				throw new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND);
			}
		}

		// FastAPI 요청 데이터 생성
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("jobName", request.getJobName());
		requestMap.put("siteType", request.getSiteType());
		requestMap.put("userId", userId.toString());

		// 후보지 목록 변환
		List<Map<String, Object>> candidates = request.getCandidates().stream()
			.map(this::convertCandidateToMap)
			.collect(Collectors.toList());
		requestMap.put("candidates", candidates);

		// 추천 기준 추가
		if (request.getCriteria() != null) {
			Map<String, Object> criteria = new HashMap<>();
			criteria.put("hazardWeights", request.getCriteria().getHazardWeights());
			criteria.put("excludedHazards", request.getCriteria().getExcludedHazards());
			criteria.put("minRiskScore", request.getCriteria().getMinRiskScore());
			criteria.put("maxRiskScore", request.getCriteria().getMaxRiskScore());
			criteria.put("additionalFilters", request.getCriteria().getAdditionalFilters());
			requestMap.put("criteria", criteria);
		}

		// 기준 사업장 ID 추가
		if (request.getReferenceSiteId() != null) {
			requestMap.put("referenceSiteId", request.getReferenceSiteId().toString());
		}

		return fastApiClient.startRecommendationBatch(requestMap)
			.map(response -> SiteRecommendationBatchResponse.builder()
				.batchJobId(UUID.fromString(response.get("batchJobId").toString()))
				.jobType(JobType.fromCode(response.get("jobType").toString()))
				.jobName(response.get("jobName").toString())
				.status(BatchStatus.fromCode(response.get("status").toString()))
				.startedAt(LocalDateTime.parse(response.get("startedAt").toString()))
				.totalCandidates(((Number) response.get("totalCandidates")).intValue())
				.progressPercentage(((Number) response.get("progressPercentage")).intValue())
				.build());
	}

	/**
	 * 배치 작업 진행 상황 조회
	 *
	 * @param batchJobId 배치 작업 ID
	 * @return 진행 상황
	 */
	public Mono<BatchProgressResponse> getBatchProgress(UUID batchJobId) {
		log.info("배치 작업 진행 상황 조회: batchJobId={}", batchJobId);

		return fastApiClient.getBatchProgress(batchJobId)
			.map(response -> BatchProgressResponse.builder()
				.batchJobId(UUID.fromString(response.get("batchJobId").toString()))
				.jobName(response.get("jobName").toString())
				.status(BatchStatus.fromCode(response.get("status").toString()))
				.progressPercentage(((Number) response.get("progressPercentage")).intValue())
				.processedItems(((Number) response.get("processedItems")).intValue())
				.totalItems(((Number) response.get("totalItems")).intValue())
				.startedAt(response.get("startedAt") != null ?
					LocalDateTime.parse(response.get("startedAt").toString()) : null)
				.completedAt(response.get("completedAt") != null ?
					LocalDateTime.parse(response.get("completedAt").toString()) : null)
				.errorMessage((String) response.get("errorMessage"))
				.metadata((Map<String, Object>) response.get("metadata"))
				.build());
	}

	/**
	 * 배치 작업 결과 조회
	 *
	 * @param batchJobId 배치 작업 ID
	 * @return 추천 결과
	 */
	public Mono<SiteRecommendationResultResponse> getRecommendationResult(UUID batchJobId) {
		log.info("배치 작업 결과 조회: batchJobId={}", batchJobId);

		return fastApiClient.getRecommendationResult(batchJobId)
			.map(response -> {
				// recommendations 배열 파싱
				List<Map<String, Object>> recommendationMaps =
					(List<Map<String, Object>>) response.get("recommendations");

				List<RecommendationItem> recommendations = recommendationMaps != null ?
					recommendationMaps.stream()
						.map(this::mapToRecommendationItem)
						.collect(Collectors.toList()) : Collections.emptyList();

				return SiteRecommendationResultResponse.builder()
					.batchJobId(UUID.fromString(response.get("batchJobId").toString()))
					.jobName(response.get("jobName").toString())
					.status(BatchStatus.fromCode(response.get("status").toString()))
					.startedAt(response.get("startedAt") != null ?
						LocalDateTime.parse(response.get("startedAt").toString()) : null)
					.completedAt(response.get("completedAt") != null ?
						LocalDateTime.parse(response.get("completedAt").toString()) : null)
					.recommendations(recommendations)
					.totalCandidates(((Number) response.get("totalCandidates")).intValue())
					.errorMessage((String) response.get("errorMessage"))
					.build();
			});
	}

	/**
	 * CandidateSite를 Map으로 변환
	 *
	 * @param candidate 후보지
	 * @return Map
	 */
	private Map<String, Object> convertCandidateToMap(CandidateSite candidate) {
		Map<String, Object> map = new HashMap<>();
		map.put("name", candidate.getName());
		map.put("latitude", candidate.getLatitude());
		map.put("longitude", candidate.getLongitude());
		map.put("roadAddress", candidate.getRoadAddress());
		map.put("jibunAddress", candidate.getJibunAddress());
		return map;
	}

	/**
	 * Map을 RecommendationItem으로 변환
	 *
	 * @param map FastAPI 응답 Map
	 * @return RecommendationItem
	 */
	private RecommendationItem mapToRecommendationItem(Map<String, Object> map) {
		return RecommendationItem.builder()
			.name((String) map.get("name"))
			.latitude(map.get("latitude") != null ?
				new java.math.BigDecimal(map.get("latitude").toString()) : null)
			.longitude(map.get("longitude") != null ?
				new java.math.BigDecimal(map.get("longitude").toString()) : null)
			.roadAddress((String) map.get("roadAddress"))
			.rank(map.get("rank") != null ? ((Number) map.get("rank")).intValue() : null)
			.overallRiskScore(map.get("overallRiskScore") != null ?
				((Number) map.get("overallRiskScore")).doubleValue() : null)
			.hazardScores((Map<String, Double>) map.get("hazardScores"))
			.recommendation((String) map.get("recommendation"))
			.analysisDetails((Map<String, Object>) map.get("analysisDetails"))
			.build();
	}
}
