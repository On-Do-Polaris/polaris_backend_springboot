package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.service.SimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 시뮬레이션 컨트롤러
 *
 * FastAPI를 통한 기후 시뮬레이션 및 사업장 이전 분석
 *
 * 최종 수정일: 2025-11-18
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

	private final SimulationService simulationService;

	/**
	 * 사업장 이전 시뮬레이션 (비교)
	 *
	 * POST /api/simulation/relocation/compare
	 *
	 * @param request 비교 요청
	 * @return 비교 결과
	 */
	@PostMapping("/relocation/compare")
	public Mono<ResponseEntity<Map<String, Object>>> compareRelocation(
		@RequestBody Map<String, Object> request
	) {
		log.info("POST /api/simulation/relocation/compare");
		return simulationService.compareRelocation(request)
			.map(ResponseEntity::ok);
	}

	/**
	 * 기후 시뮬레이션
	 *
	 * POST /api/simulation/climate
	 *
	 * @param request 시뮬레이션 요청
	 * @return 시뮬레이션 결과
	 */
	@PostMapping("/climate")
	public Mono<ResponseEntity<Map<String, Object>>> runClimateSimulation(
		@RequestBody Map<String, Object> request
	) {
		log.info("POST /api/simulation/climate");
		return simulationService.runClimateSimulation(request)
			.map(ResponseEntity::ok);
	}
}
