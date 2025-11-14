package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.domain.meta.entity.HazardType;
import com.skax.physicalrisk.domain.meta.entity.Industry;
import com.skax.physicalrisk.service.MetaService;
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
	@GetMapping("/ssp-scenarios")
	public ResponseEntity<List<String>> getSspScenarios() {
		log.info("GET /api/meta/ssp-scenarios - Fetching SSP scenarios");
		List<String> scenarios = List.of("SSP1-2.6", "SSP2-4.5", "SSP5-8.5");
		return ResponseEntity.ok(scenarios);
	}
}

