package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.domain.meta.entity.HazardType;
import com.skax.physicalrisk.domain.meta.entity.Industry;
import com.skax.physicalrisk.service.meta.MetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 메타 데이터 컨트롤러
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/meta")
@RequiredArgsConstructor
public class MetaController {

	private final MetaService metaService;

	/**
	 * 위험 유형 목록 조회
	 *
	 * @return 위험 유형 목록
	 */
	@Operation(
		summary = "재해 유형 목록 조회",
		description = "재해 유형(HazardType) 코드/이름/카테고리 목록 조회."
	)
	@ApiResponse(
		responseCode = "200",
		description = "재해 유형 목록",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = List.class),
			examples = @ExampleObject(
				value = "[{\"id\": 1, \"code\": \"extreme_heat\", \"name\": \"극심한 고온\", \"nameEn\": \"Extreme Heat\", \"category\": \"TEMPERATURE\", \"description\": \"폭염 및 열파로 인한 위험\"}, {\"id\": 2, \"code\": \"extreme_cold\", \"name\": \"극심한 한파\", \"nameEn\": \"Extreme Cold\", \"category\": \"TEMPERATURE\", \"description\": \"한파 및 동파로 인한 위험\"}]"
			)
		)
	)
	@GetMapping("/hazards")
	public ResponseEntity<List<HazardType>> getHazardTypes() {
		log.info("GET /api/meta/hazards - Fetching hazard types");
		List<HazardType> hazardTypes = metaService.getAllHazardTypes();
		return ResponseEntity.ok(hazardTypes);
	}

	/**
	 * 산업 분류 목록 조회
	 *
	 * @return 산업 분류 목록
	 */
	@Operation(
		summary = "산업 분류 목록 조회",
		description = "산업(Industry) 코드/이름/설명 목록 조회."
	)
	@ApiResponse(
		responseCode = "200",
		description = "산업 분류 목록",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = List.class),
			examples = @ExampleObject(
				value = "[{\"id\": 1, \"code\": \"data_center\", \"name\": \"데이터센터\", \"description\": \"서버 및 IT 인프라 운영 시설\"}, {\"id\": 2, \"code\": \"manufacturing\", \"name\": \"제조업\", \"description\": \"공장 및 생산 시설\"}]"
			)
		)
	)
	@GetMapping("/industries")
	public ResponseEntity<List<Industry>> getIndustries() {
		log.info("GET /api/meta/industries - Fetching industries");
		List<Industry> industries = metaService.getAllIndustries();
		return ResponseEntity.ok(industries);
	}

	/**
	 * SSP 시나리오 목록 조회
	 *
	 * @return SSP 시나리오 목록
	 */
	@Operation(
		summary = "SSP 시나리오 목록 조회",
		description = "SSP(공통사회경제경로) 시나리오 코드 목록 조회."
	)
	@ApiResponse(
		responseCode = "200",
		description = "SSP 시나리오 코드 목록",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = List.class),
			examples = @ExampleObject(
				value = "[\"SSP1-2.6\", \"SSP2-4.5\", \"SSP3-7.0\", \"SSP5-8.5\"]"
			)
		)
	)
	@GetMapping("/ssp-scenarios")
	public ResponseEntity<List<String>> getSspScenarios() {
		log.info("GET /api/meta/ssp-scenarios - Fetching SSP scenarios");
		List<String> scenarios = List.of(
			"SSP1-2.6",  // Sustainability
			"SSP2-4.5",  // Middle of the Road
			"SSP3-7.0",  // Regional Rivalry
			"SSP5-8.5"   // Fossil-fueled Development
		);
		return ResponseEntity.ok(scenarios);
	}
}

