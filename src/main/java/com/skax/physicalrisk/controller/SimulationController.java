package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.simulation.ClimateSimulationRequest;
import com.skax.physicalrisk.dto.request.simulation.RelocationSimulationRequest;
import com.skax.physicalrisk.dto.response.simulation.ClimateSimulationResponse;
import com.skax.physicalrisk.dto.response.simulation.RelocationSimulationResponse;
import com.skax.physicalrisk.service.simulation.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 시뮬레이션 컨트롤러
 *
 * FastAPI를 통한 기후 시뮬레이션 및 사업장 이전 분석
 *
 * 최종 수정일: 2025-11-20
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@Tag(name = "시뮬레이션", description = "기후 시뮬레이션 및 사업장 이전 분석 API")
public class SimulationController {

	private final SimulationService simulationService;

	/**
	 * 사업장 이전 시뮬레이션 (비교)
	 *
	 * POST /api/simulation/relocation/compare
	 *
	 * @param request 이전 시뮬레이션 요청
	 * @return 비교 결과
	 */
	@PostMapping("/relocation/compare")
	public ResponseEntity<RelocationSimulationResponse> compareRelocation(
		@Valid @RequestBody RelocationSimulationRequest request
	) {
		log.info("POST /api/simulation/relocation/compare");
		RelocationSimulationResponse response = simulationService.compareRelocation(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 기후 시뮬레이션
	 *
	 * POST /api/simulation/climate
	 *
	 * @param request 기후 시뮬레이션 요청
	 * @return 시뮬레이션 결과
	 */
	@PostMapping("/climate")
	@Operation(
		summary = "기후 시뮬레이션 실행",
		description = "현재 사용자의 모든 사업장에 대해 SSP 시나리오 기반 기후 변화 시뮬레이션을 실행합니다 (2020-2100년)"
	)
	public ResponseEntity<ClimateSimulationResponse> runClimateSimulation(
		@Valid @RequestBody ClimateSimulationRequest request
	) {
		log.info("POST /api/simulation/climate");
		ClimateSimulationResponse response = simulationService.runClimateSimulation(request);
		return ResponseEntity.ok(response);
	}
}
