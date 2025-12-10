package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.domain.site.entity.DataCategory;
import com.skax.physicalrisk.dto.request.site.AdditionalDataInput;
import com.skax.physicalrisk.dto.response.site.AdditionalDataGetResponse;
import com.skax.physicalrisk.dto.response.site.AdditionalDataUploadResponse;
import com.skax.physicalrisk.service.site.AdditionalDataService;
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

import java.util.Map;
import java.util.UUID;

/**
 * 추가 데이터 관리 컨트롤러
 *
 * 최종 수정일: 2025-12-09
 * 파일 버전: v01 - ERD 기준 추가 데이터 API
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/additional-data")
@RequiredArgsConstructor
@Tag(name = "추가 데이터 관리", description = "사업장 추가 데이터 업로드 및 조회 API")
public class AdditionalDataController {

	private final AdditionalDataService additionalDataService;

	/**
	 * 추가 데이터 업로드
	 *
	 * @param siteId 사업장 ID (쿼리 파라미터)
	 * @param input 추가 데이터 입력
	 * @return 업로드 응답
	 */
	@PostMapping
	@Operation(summary = "추가 데이터 업로드", description = "사업장에 추가 데이터(건물정보, 자산정보 등)를 업로드합니다")
	public Mono<ResponseEntity<AdditionalDataUploadResponse>> uploadAdditionalData(
		@Parameter(description = "사업장 ID", required = true) @RequestParam UUID siteId,
		@Valid @RequestBody AdditionalDataInput input
	) {
		log.info("POST /api/additional-data?siteId={} - dataCategory: {}", siteId, input.getDataCategory());
		return additionalDataService.uploadAdditionalData(siteId, input)
			.map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
	}

	/**
	 * 추가 데이터 조회
	 *
	 * @param siteId 사업장 ID
	 * @param dataCategory 데이터 카테고리
	 * @return 추가 데이터 응답
	 */
	@GetMapping
	@Operation(summary = "추가 데이터 조회", description = "특정 카테고리의 추가 데이터를 조회합니다")
	public Mono<ResponseEntity<AdditionalDataGetResponse>> getAdditionalData(
		@Parameter(description = "사업장 ID", required = true) @RequestParam UUID siteId,
		@Parameter(description = "데이터 카테고리 (building/asset/power/insurance/custom)", required = true)
		@RequestParam String dataCategory
	) {
		log.info("GET /api/additional-data?siteId={}&dataCategory={}", siteId, dataCategory);
		DataCategory category = DataCategory.fromCode(dataCategory);
		return additionalDataService.getAdditionalData(siteId, category)
			.map(ResponseEntity::ok);
	}

	/**
	 * 추가 데이터 삭제
	 *
	 * @param siteId 사업장 ID
	 * @param dataId 데이터 ID
	 * @return 삭제 성공 여부
	 */
	@DeleteMapping
	@Operation(summary = "추가 데이터 삭제", description = "특정 추가 데이터를 삭제합니다")
	public Mono<ResponseEntity<Map<String, Object>>> deleteAdditionalData(
		@Parameter(description = "사업장 ID", required = true) @RequestParam UUID siteId,
		@Parameter(description = "데이터 ID", required = true) @RequestParam UUID dataId
	) {
		log.info("DELETE /api/additional-data?siteId={}&dataId={}", siteId, dataId);
		return additionalDataService.deleteAdditionalData(siteId, dataId)
			.map(success -> ResponseEntity.ok(Map.of("success", success, "message", "삭제되었습니다")));
	}

	/**
	 * 정형화된 데이터 조회
	 *
	 * @param siteId 사업장 ID
	 * @param dataId 데이터 ID
	 * @return 정형화된 데이터
	 */
	@GetMapping("/structured")
	@Operation(summary = "정형화된 데이터 조회", description = "AI가 추출한 정형화된 데이터를 조회합니다")
	public Mono<ResponseEntity<Map<String, Object>>> getStructuredData(
		@Parameter(description = "사업장 ID", required = true) @RequestParam UUID siteId,
		@Parameter(description = "데이터 ID", required = true) @RequestParam UUID dataId
	) {
		log.info("GET /api/additional-data/structured?siteId={}&dataId={}", siteId, dataId);
		return additionalDataService.getStructuredData(siteId, dataId)
			.map(ResponseEntity::ok);
	}
}
