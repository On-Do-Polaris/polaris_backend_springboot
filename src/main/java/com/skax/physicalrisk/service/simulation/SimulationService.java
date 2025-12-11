package com.skax.physicalrisk.service.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skax.physicalrisk.client.fastapi.FastApiClient;
import com.skax.physicalrisk.client.fastapi.dto.AalAnalysisData;
import com.skax.physicalrisk.domain.site.entity.Site;
import com.skax.physicalrisk.domain.site.repository.SiteRepository;
import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.dto.request.simulation.ClimateSimulationRequest;
import com.skax.physicalrisk.dto.request.simulation.RelocationSimulationRequest;
import com.skax.physicalrisk.dto.response.simulation.ClimateSimulationResponse;
import com.skax.physicalrisk.dto.response.simulation.RelocationSimulationResponse;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import com.skax.physicalrisk.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 시뮬레이션 서비스
 *
 * FastAPI 서버를 통한 기후 시뮬레이션 및 사업장 이전 분석
 *
 * 최종 수정일: 2025-11-25
 * 파일 버전: v03 (AAL v11 지원)
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimulationService {

	private final FastApiClient fastApiClient;
	private final SiteRepository siteRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	/**
	 * 위치 시뮬레이션 후보지 조회
	 *
	 * @param siteId 사업장 ID
	 * @return 추천 후보지 3개 및 리스크 정보
	 */
	public com.skax.physicalrisk.dto.response.simulation.LocationRecommendationResponse getLocationRecommendation(String siteId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Getting location recommendation for siteId={}, userId={}", siteId, userId);

		// 사용자 조회 및 검증
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// 사업장 조회 및 권한 검증
		Site site = siteRepository.findById(UUID.fromString(siteId))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND));

		if (!site.getUser().getId().equals(userId)) {
			throw new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND, "해당 사업장에 대한 권한이 없습니다");
		}

		Map<String, Object> response = fastApiClient.getLocationRecommendation(siteId).block();
		return convertToLocationRecommendationResponse(response);
	}

	/**
	 * 위치 시뮬레이션 비교 (현재지 vs 신규 후보지)
	 * AAL v11: aal_analysis에서 final_aal_percentage를 가져와 0-1 스케일로 변환
	 *
	 * @param request 비교 시뮬레이션 요청
	 * @return 비교 결과
	 */
	public RelocationSimulationResponse compareLocation(RelocationSimulationRequest request) {
		log.info("Comparing location: currentSiteId={}, newLocation=({}, {})",
			request.getCurrentSiteId(), request.getLatitude(), request.getLongitude());

		// DTO를 Map으로 변환
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("currentSiteId", request.getCurrentSiteId());
		requestMap.put("latitude", request.getLatitude());
		requestMap.put("longitude", request.getLongitude());
		requestMap.put("roadAddress", request.getRoadAddress());
		requestMap.put("jibunAddress", request.getJibunAddress());

		Map<String, Object> response = fastApiClient.compareRelocation(requestMap).block();

		// AAL v11: aal_analysis 필드에서 AAL 데이터 추출 및 변환
		return convertToRelocationResponse(response);
	}

	/**
	 * 기후 시뮬레이션
	 *
	 * 현재 사용자의 모든 사업장에 대해 2020-2100년 기간의 시뮬레이션을 자동 실행
	 *
	 * @param request 기후 시뮬레이션 요청 (scenario, hazardType만 포함)
	 * @return 시뮬레이션 결과
	 */
	public ClimateSimulationResponse runClimateSimulation(ClimateSimulationRequest request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Running climate simulation for user: {}, scenario={}, hazardType={}",
			userId, request.getScenario(), request.getHazardType());

		// 사용자 조회 및 검증
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// 사용자의 모든 사업장 조회
		List<Site> sites = siteRepository.findByUser(user);
		if (sites.isEmpty()) {
			throw new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND, "사용자의 사업장이 없습니다");
		}

		// 사업장 ID 목록 추출
		List<UUID> siteIds = sites.stream()
			.map(Site::getId)
			.collect(Collectors.toList());

		log.info("Fetched {} sites for climate simulation", siteIds.size());

		// FastAPI 요청 데이터 생성 (모든 사업장 + 전체 연도 범위)
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("scenario", request.getScenario());
		requestMap.put("hazardType", request.getHazardType());
		requestMap.put("siteIds", siteIds);
		requestMap.put("startYear", 2020);  // 전체 기간: 2020-2100

		// FastAPI 호출
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

	/**
	 * AAL v11 응답을 RelocationSimulationResponse로 변환
	 * physical_risk_scores와 aal_analysis를 결합하여 최종 응답 생성
	 *
	 * @param response FastAPI 응답 Map
	 * @return 변환된 RelocationSimulationResponse
	 */
	@SuppressWarnings("unchecked")
	private RelocationSimulationResponse convertToRelocationResponse(Map<String, Object> response) {
		try {
			RelocationSimulationResponse result = new RelocationSimulationResponse();

			// currentLocation 처리
			Map<String, Object> currentLoc = (Map<String, Object>) response.get("currentLocation");
			if (currentLoc != null) {
				result.setCurrentLocation(convertLocationData(currentLoc));
			}

			// newLocation 처리
			Map<String, Object> newLoc = (Map<String, Object>) response.get("newLocation");
			if (newLoc != null) {
				result.setNewLocation(convertLocationData(newLoc));
			}

			return result;
		} catch (Exception e) {
			log.error("Failed to convert relocation response: {}", e.getMessage());
			throw new RuntimeException("이전 시뮬레이션 응답 변환 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * LocationData 변환
	 * AAL v11: aal_analysis에서 final_aal_percentage를 추출하여 AAL 설정
	 *
	 * @param locationMap FastAPI 응답의 location 맵
	 * @return 변환된 LocationData
	 */
	@SuppressWarnings("unchecked")
	private RelocationSimulationResponse.LocationData convertLocationData(Map<String, Object> locationMap) {
		RelocationSimulationResponse.LocationData locationData = new RelocationSimulationResponse.LocationData();

		// physical_risk_scores 가져오기
		Map<String, Object> physicalRiskScores = (Map<String, Object>) locationMap.get("physical_risk_scores");

		// AAL v11: aal_analysis 가져오기
		Map<String, Object> aalAnalysis = (Map<String, Object>) locationMap.get("aal_analysis");

		if (physicalRiskScores != null && aalAnalysis != null) {
			java.util.List<RelocationSimulationResponse.RiskData> risks = new java.util.ArrayList<>();

			// 각 리스크 타입에 대해 physical_risk_score와 AAL을 결합
			for (Map.Entry<String, Object> entry : physicalRiskScores.entrySet()) {
				String riskType = entry.getKey();
				Map<String, Object> riskData = (Map<String, Object>) entry.getValue();

				// Physical Risk Score 추출
				Object scoreObj = riskData.get("physical_risk_score_100");
				Integer riskScore = scoreObj != null ? ((Number) scoreObj).intValue() : 0;

				// AAL v11: aal_analysis에서 final_aal_percentage 추출
				Double aal = 0.0;
				Map<String, Object> aalData = (Map<String, Object>) aalAnalysis.get(riskType);
				if (aalData != null) {
					Object finalAalObj = aalData.get("final_aal_percentage");
					if (finalAalObj != null) {
						Double finalAalPercentage = ((Number) finalAalObj).doubleValue();
						// % → 0-1 스케일 변환
						aal = finalAalPercentage / 100.0;
						log.debug("Risk type: {}, AAL: {}% -> {}", riskType, finalAalPercentage, aal);
					}
				}

				RelocationSimulationResponse.RiskData risk = RelocationSimulationResponse.RiskData.builder()
					.riskType(convertRiskTypeName(riskType))
					.riskScore(riskScore)
					.aal(aal)
					.build();

				risks.add(risk);
			}

			locationData.setRisks(risks);
		}

		return locationData;
	}

	/**
	 * 리스크 타입 이름 변환 (영문 → 한글)
	 *
	 * @param riskType 영문 리스크 타입
	 * @return 한글 리스크 타입
	 */
	private String convertRiskTypeName(String riskType) {
		Map<String, String> riskNames = Map.of(
			"extreme_heat", "극심한 고온",
			"typhoon", "태풍",
			"flood", "홍수",
			"drought", "가뭄",
			"wildfire", "산불",
			"sea_level_rise", "해수면 상승"
		);
		return riskNames.getOrDefault(riskType, riskType);
	}

	/**
	 * FastAPI 응답을 LocationRecommendationResponse로 변환
	 *
	 * @param response FastAPI 응답 Map
	 * @return 변환된 LocationRecommendationResponse
	 */
	@SuppressWarnings("unchecked")
	private com.skax.physicalrisk.dto.response.simulation.LocationRecommendationResponse convertToLocationRecommendationResponse(Map<String, Object> response) {
		try {
			return objectMapper.convertValue(response, com.skax.physicalrisk.dto.response.simulation.LocationRecommendationResponse.class);
		} catch (Exception e) {
			log.error("Failed to convert location recommendation response: {}", e.getMessage());
			throw new RuntimeException("위치 추천 응답 변환 실패: " + e.getMessage(), e);
		}
	}
}
