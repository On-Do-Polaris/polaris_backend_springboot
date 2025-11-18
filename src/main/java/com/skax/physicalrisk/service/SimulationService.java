package com.skax.physicalrisk.service;

import com.skax.physicalrisk.client.fastapi.FastApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 시뮬레이션 서비스
 *
 * FastAPI 서버를 통한 기후 시뮬레이션 및 사업장 이전 분석
 *
 * 최종 수정일: 2025-11-18
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimulationService {

	private final FastApiClient fastApiClient;

	/**
	 * 사업장 이전 후보지 추천
	 *
	 * @param request 후보지 요청 (siteId, hazardTypes, targetRegion 등)
	 * @return 후보지 목록
	 */
	public Mono<Map<String, Object>> getRelocationCandidates(Map<String, Object> request) {
		log.info("Fetching relocation candidates with request: {}", request);
		return fastApiClient.getRelocationCandidates(request);
	}

	/**
	 * 사업장 이전 시뮬레이션 (현재지 vs 후보지 비교)
	 *
	 * @param request 비교 요청 (currentSiteId, candidateSiteId 등)
	 * @return 비교 결과
	 */
	public Mono<Map<String, Object>> compareRelocation(Map<String, Object> request) {
		log.info("Comparing relocation with request: {}", request);
		return fastApiClient.compareRelocation(request);
	}

	/**
	 * 기후 시뮬레이션
	 *
	 * @param request 시뮬레이션 요청 (siteId, scenario, timeRange 등)
	 * @return 시뮬레이션 결과
	 */
	public Mono<Map<String, Object>> runClimateSimulation(Map<String, Object> request) {
		log.info("Running climate simulation with request: {}", request);
		return fastApiClient.runClimateSimulation(request);
	}
}
