package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.recommendation.SiteRecommendationBatchRequest;
import com.skax.physicalrisk.dto.response.recommendation.BatchProgressResponse;
import com.skax.physicalrisk.dto.response.recommendation.SiteRecommendationBatchResponse;
import com.skax.physicalrisk.dto.response.recommendation.SiteRecommendationResultResponse;
import com.skax.physicalrisk.service.recommendation.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 후보지 추천 컨트롤러
 *
 * 최종 수정일: 2025-12-09
 * 파일 버전: v01 - ERD 기준 후보지 추천 배치 API
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
@Tag(name = "후보지 추천", description = "후보지 추천 배치 작업 API")
public class RecommendationController {

	private final RecommendationService recommendationService;

	/**
	 * 후보지 추천 배치 시작
	 *
	 * @param request 배치 요청
	 * @return 배치 작업 응답
	 */
	@PostMapping
	@Operation(
		summary = "후보지 추천 배치 시작",
		description = "여러 후보지에 대한 기후 위험 분석 및 추천을 배치로 시작합니다"
	)
	public Mono<ResponseEntity<SiteRecommendationBatchResponse>> startRecommendationBatch(
		@Valid @RequestBody SiteRecommendationBatchRequest request
	) {
		log.info("POST /api/recommendation - jobName: {}, candidatesCount: {}",
			request.getJobName(), request.getCandidates().size());

		return recommendationService.startRecommendationBatch(request)
			.map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
	}

	/**
	 * 배치 작업 진행 상황 조회
	 *
	 * @param batchJobId 배치 작업 ID
	 * @return 진행 상황
	 */
	@GetMapping("/{batchJobId}/progress")
	@Operation(
		summary = "배치 작업 진행 상황 조회",
		description = "배치 작업의 현재 진행 상황을 조회합니다"
	)
	public Mono<ResponseEntity<BatchProgressResponse>> getBatchProgress(
		@Parameter(description = "배치 작업 ID", required = true) @PathVariable UUID batchJobId
	) {
		log.info("GET /api/recommendation/{}/progress", batchJobId);

		return recommendationService.getBatchProgress(batchJobId)
			.map(ResponseEntity::ok);
	}

	/**
	 * 배치 작업 결과 조회
	 *
	 * @param batchJobId 배치 작업 ID
	 * @return 추천 결과
	 */
	@GetMapping("/{batchJobId}/result")
	@Operation(
		summary = "배치 작업 결과 조회",
		description = "완료된 배치 작업의 후보지 추천 결과를 조회합니다"
	)
	public Mono<ResponseEntity<SiteRecommendationResultResponse>> getRecommendationResult(
		@Parameter(description = "배치 작업 ID", required = true) @PathVariable UUID batchJobId
	) {
		log.info("GET /api/recommendation/{}/result", batchJobId);

		return recommendationService.getRecommendationResult(batchJobId)
			.map(ResponseEntity::ok);
	}
}
