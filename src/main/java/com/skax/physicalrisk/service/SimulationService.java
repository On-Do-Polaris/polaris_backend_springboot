package com.skax.physicalrisk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skax.physicalrisk.client.fastapi.FastApiClient;
import com.skax.physicalrisk.dto.request.simulation.ClimateSimulationRequest;
import com.skax.physicalrisk.dto.request.simulation.RelocationSimulationRequest;
import com.skax.physicalrisk.dto.response.simulation.ClimateSimulationResponse;
import com.skax.physicalrisk.dto.response.simulation.RelocationSimulationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 시뮬레이션 서비스
 *
 * FastAPI 서버를 통한 기후 시뮬레이션 및 사업장 이전 분석
 *
 * 최종 수정일: 2025-11-20
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimulationService {

	private final FastApiClient fastApiClient;
	private final ObjectMapper objectMapper;

	/**
	 * 사업장 이전 시뮬레이션 (현재지 vs 이전지 비교)
	 *
	 * @param request 이전 시뮬레이션 요청
	 * @return 비교 결과
	 */
	public RelocationSimulationResponse compareRelocation(RelocationSimulationRequest request) {
		log.info("Comparing relocation: baseSiteId={}, newLocation=({}, {})",
			request.getBaseSiteId(), request.getLatitude(), request.getLongitude());

		// DTO를 Map으로 변환
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("baseSiteId", request.getBaseSiteId());
		requestMap.put("latitude", request.getLatitude());
		requestMap.put("longitude", request.getLongitude());
		requestMap.put("roadAddress", request.getRoadAddress());
		requestMap.put("jibunAddress", request.getJibunAddress());

		Map<String, Object> response = fastApiClient.compareRelocation(requestMap).block();
		return convertToDto(response, RelocationSimulationResponse.class);
	}

	/**
	 * 기후 시뮬레이션
	 *
	 * @param request 기후 시뮬레이션 요청
	 * @return 시뮬레이션 결과
	 */
	public ClimateSimulationResponse runClimateSimulation(ClimateSimulationRequest request) {
		log.info("Running climate simulation: scenario={}, hazardType={}",
			request.getScenario(), request.getHazardType());

		// DTO를 Map으로 변환
		Map<String, Object> requestMap = objectMapper.convertValue(request, Map.class);

		Map<String, Object> response = fastApiClient.runClimateSimulation(requestMap).block();
		return convertToDto(response, ClimateSimulationResponse.class);
	}

	/**
	 * Map을 DTO로 변환
	 */
	private <T> T convertToDto(Map<String, Object> map, Class<T> clazz) {
		try {
			return objectMapper.convertValue(map, clazz);
		} catch (Exception e) {
			log.error("Failed to convert response to {}: {}", clazz.getSimpleName(), e.getMessage());
			throw new RuntimeException("응답 변환 실패: " + e.getMessage(), e);
		}
	}
}
