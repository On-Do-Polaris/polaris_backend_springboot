package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.response.disaster.DisasterHistoryListResponse;
import com.skax.physicalrisk.service.disaster.DisasterHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 재해 이력 컨트롤러
 *
 * 최종 수정일: 2025-12-09
 * 파일 버전: v01 - ERD 기준 재해 이력 API
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/disaster-history")
@RequiredArgsConstructor
@Tag(name = "재해 이력", description = "재해연보 데이터 조회 API")
public class DisasterHistoryController {

	private final DisasterHistoryService disasterHistoryService;

	/**
	 * 재해 이력 목록 조회
	 *
	 * @param adminCode 행정구역 코드 (옵션)
	 * @param year 연도 (옵션)
	 * @param disasterType 재해 유형 (옵션)
	 * @param page 페이지 번호 (1부터 시작, 기본값: 1)
	 * @param pageSize 페이지당 개수 (기본값: 20)
	 * @return 재해 이력 목록 응답
	 */
	@GetMapping
	@Operation(
		summary = "재해 이력 목록 조회",
		description = "재해연보 데이터를 조회합니다. 행정구역 코드, 연도, 재해 유형으로 필터링 가능합니다."
	)
	public Mono<ResponseEntity<DisasterHistoryListResponse>> getDisasterHistory(
		@Parameter(description = "행정구역 코드 (예: 11110)")
		@RequestParam(required = false) String adminCode,

		@Parameter(description = "연도 (예: 2023)")
		@RequestParam(required = false) Integer year,

		@Parameter(description = "재해 유형 (TYPHOON/HEAVY_RAIN/HEAVY_SNOW/STRONG_WIND/WIND_WAVE/EARTHQUAKE/OTHER)")
		@RequestParam(required = false) String disasterType,

		@Parameter(description = "페이지 번호 (1부터 시작)")
		@RequestParam(required = false, defaultValue = "1") Integer page,

		@Parameter(description = "페이지당 개수")
		@RequestParam(required = false, defaultValue = "20") Integer pageSize
	) {
		log.info("GET /api/disaster-history - adminCode: {}, year: {}, disasterType: {}, page: {}, pageSize: {}",
			adminCode, year, disasterType, page, pageSize);

		return disasterHistoryService.getDisasterHistory(adminCode, year, disasterType, page, pageSize)
			.map(ResponseEntity::ok);
	}
}
