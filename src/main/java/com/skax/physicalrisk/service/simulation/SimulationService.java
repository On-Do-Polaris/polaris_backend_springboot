package com.skax.physicalrisk.service.simulation;
// 반드시 이 패키지여야 합니다.
import com.fasterxml.jackson.core.type.TypeReference;
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
			request.getSiteId(),
			request.getCandidate().getLatitude(),
			request.getCandidate().getLongitude());

		// DTO를 Map으로 변환
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("currentSiteId", request.getSiteId());
		requestMap.put("latitude", request.getCandidate().getLatitude());
		requestMap.put("longitude", request.getCandidate().getLongitude());
		requestMap.put("roadAddress", request.getCandidate().getRoadAddress());
		requestMap.put("jibunAddress", request.getCandidate().getJibunAddress());

		Map<String, Object> response = fastApiClient.compareRelocation(requestMap).block();

		// AAL v11: aal_analysis 필드에서 AAL 데이터 추출 및 변환
		return convertToRelocationResponse(response);
	}

/**
     * 기후 시뮬레이션 실행
     *
     * 1. DB에서 사용자 사업장 정보 조회 (이름, 지역코드 등)
     * 2. FastAPI에 연산 요청 (Site ID만 전달)
     * 3. 결과 병합 (DB 정보 + FastAPI 연산 결과)
     */
    public ClimateSimulationResponse runClimateSimulation(ClimateSimulationRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Running climate simulation for user: {}, scenario={}, hazardType={}",
            userId, request.getScenario(), request.getHazardType());

        // 1. 사용자 및 사업장 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        List<Site> sites = siteRepository.findByUser(user);
        if (sites.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND, "사용자의 사업장이 없습니다");
        }

        // 2. FastAPI 요청 데이터 생성
        List<UUID> siteIds = sites.stream().map(Site::getId).collect(Collectors.toList());

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("scenario", request.getScenario());
        requestMap.put("hazardType", request.getHazardType());
        requestMap.put("siteIds", siteIds);
        requestMap.put("startYear", 2025); // 요구사항에 맞춰 2025년으로 변경 (기존 2020)
        requestMap.put("endYear", 2100);

        log.info("Calling FastAPI climate simulation: scenario={}, hazardType={}, siteCount={}",
            request.getScenario(), request.getHazardType(), siteIds.size());
        log.debug("FastAPI request: {}", requestMap);

        // 3. FastAPI 호출 (비동기 결과를 동기로 대기)
        // 예상 FastAPI 응답 구조:
        // {
        //   "regionScores": { "11010": { "2025": 45.2, ... } },
        //   "siteAALs": { "uuid-string": { "2025": 12.5, ... } }
        // }
        Map<String, Object> apiResponse = fastApiClient.runClimateSimulation(requestMap).block();

        if (apiResponse == null) {
            log.error("FastAPI returned null response");
            throw new RuntimeException("FastAPI로부터 응답을 받지 못했습니다.");
        }

        log.info("Received FastAPI response with keys: {}", apiResponse.keySet());

        // 4. 응답 데이터 조립 (DB 데이터 + API 결과 병합)
        return buildSimulationResponse(request, sites, apiResponse);
    }

    /**
     * DB의 사업장 정보와 FastAPI의 계산 결과를 병합하여 최종 DTO 생성
     */
    private ClimateSimulationResponse buildSimulationResponse(
            ClimateSimulationRequest request,
            List<Site> sites,
            Map<String, Object> apiResponse
    ) {
        log.info("Building simulation response from FastAPI data");
        log.debug("FastAPI response keys: {}", apiResponse.keySet());

        // 4-1. 행정구역 점수 파싱 (regionScores)
        Map<String, Map<String, Double>> regionScores = new HashMap<>();
        if (apiResponse.containsKey("regionScores") && apiResponse.get("regionScores") != null) {
            try {
                regionScores = objectMapper.convertValue(
                    apiResponse.get("regionScores"),
                    new TypeReference<Map<String, Map<String, Double>>>() {}
                );
                log.info("Parsed regionScores: {} regions", regionScores.size());
            } catch (Exception e) {
                log.error("Failed to parse regionScores: {}", e.getMessage(), e);
            }
        } else {
            log.warn("regionScores not found in FastAPI response");
        }

        // 4-2. 사업장별 AAL 결과 파싱 (Key: SiteId String, Value: Map<Year, Score>)
        Map<String, Map<String, Double>> siteAalResults = new HashMap<>();
        if (apiResponse.containsKey("siteAALs") && apiResponse.get("siteAALs") != null) {
            try {
                siteAalResults = objectMapper.convertValue(
                    apiResponse.get("siteAALs"),
                    new TypeReference<Map<String, Map<String, Double>>>() {}
                );
                log.info("Parsed siteAALs: {} sites", siteAalResults.size());
                log.debug("Site AAL keys: {}", siteAalResults.keySet());
            } catch (Exception e) {
                log.error("Failed to parse siteAALs: {}", e.getMessage(), e);
            }
        } else {
            log.warn("siteAALs not found in FastAPI response");
        }

        // 4-3. Sites 리스트 조립 (DB의 이름/지역코드 + API의 AAL 값)
        List<ClimateSimulationResponse.SiteSimulationData> siteDataList = sites.stream()
        	.map(site -> {
            	String siteIdStr = site.getId().toString();
            	Map<String, Double> aalData = siteAalResults.getOrDefault(siteIdStr, new HashMap<>());

                log.debug("Site {}: found {} AAL data points", siteIdStr, aalData.size());

                return ClimateSimulationResponse.SiteSimulationData.builder()
                        .siteId(site.getId())
                        .siteName(site.getName())        // DB에서 가져온 이름
                        .regionCode(site.getRegionCode()) // DB에서 가져온 지역코드
                        .aalByYear(aalData)              // API에서 가져온 연산 결과
                        .build();
			})
            .collect(Collectors.toList());

        log.info("Built simulation response with {} sites", siteDataList.size());

        // 4-4. 최종 DTO 반환
        return ClimateSimulationResponse.builder()
                .scenario(request.getScenario())
                .hazardType(request.getHazardType())
                .regionScores(regionScores)
                .sites(siteDataList)
                .build();
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

			// siteId 설정
			Object siteIdObj = response.get("siteId");
			if (siteIdObj != null) {
				result.setSiteId(UUID.fromString(siteIdObj.toString()));
			}

			// candidate 객체 처리 (newLocation 데이터 기반)
			Map<String, Object> newLoc = (Map<String, Object>) response.get("newLocation");
			if (newLoc != null) {
				result.setCandidate(convertToCandidate(newLoc));
				result.setNewLocation(convertLocationData(newLoc));
			}

			// currentLocation 처리
			Map<String, Object> currentLoc = (Map<String, Object>) response.get("currentLocation");
			if (currentLoc != null) {
				result.setCurrentLocation(convertLocationData(currentLoc));
			}

			return result;
		} catch (Exception e) {
			log.error("Failed to convert relocation response: {}", e.getMessage());
			throw new RuntimeException("이전 시뮬레이션 응답 변환 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * FastAPI newLocation 데이터를 Candidate 객체로 변환
	 * 9개 기후 리스크 타입의 physical-risk-scores와 aal-scores를 모두 포함
	 */
	@SuppressWarnings("unchecked")
	private RelocationSimulationResponse.Candidate convertToCandidate(Map<String, Object> newLoc) {
		RelocationSimulationResponse.Candidate candidate = new RelocationSimulationResponse.Candidate();

		// 기본 정보
		Object candidateIdObj = newLoc.get("candidateId");
		if (candidateIdObj != null) {
			candidate.setCandidateId(UUID.fromString(candidateIdObj.toString()));
		}

		Object latObj = newLoc.get("latitude");
		if (latObj != null) {
			candidate.setLatitude(new java.math.BigDecimal(latObj.toString()));
		}

		Object lonObj = newLoc.get("longitude");
		if (lonObj != null) {
			candidate.setLongitude(new java.math.BigDecimal(lonObj.toString()));
		}

		candidate.setJibunAddress((String) newLoc.get("jibunAddress"));
		candidate.setRoadAddress((String) newLoc.get("roadAddress"));
		candidate.setPros((String) newLoc.get("pros"));
		candidate.setCons((String) newLoc.get("cons"));

		// riskscore, aalscore
		Object riskscoreObj = newLoc.get("riskscore");
		if (riskscoreObj != null) {
			candidate.setRiskscore(((Number) riskscoreObj).intValue());
		}

		Object aalscoreObj = newLoc.get("aalscore");
		if (aalscoreObj != null) {
			candidate.setAalscore(((Number) aalscoreObj).intValue());
		}

		// physical_risk_scores와 aal_analysis를 9개 기후 리스크로 변환
		Map<String, Object> physicalRiskScores = (Map<String, Object>) newLoc.get("physical_risk_scores");
		Map<String, Object> aalAnalysis = (Map<String, Object>) newLoc.get("aal_analysis");

		if (physicalRiskScores != null) {
			Map<String, Integer> physicalScoresMap = new java.util.HashMap<>();
			Map<String, Integer> aalScoresMap = new java.util.HashMap<>();

			// 9개 기후 리스크 타입 처리
			for (Map.Entry<String, Object> entry : physicalRiskScores.entrySet()) {
				String riskType = entry.getKey();
				Map<String, Object> riskData = (Map<String, Object>) entry.getValue();

				// Physical Risk Score 추출
				Object scoreObj = riskData.get("physical_risk_score_100");
				if (scoreObj != null) {
					physicalScoresMap.put(riskType, ((Number) scoreObj).intValue());
				}

				// AAL 추출
				if (aalAnalysis != null) {
					Map<String, Object> aalData = (Map<String, Object>) aalAnalysis.get(riskType);
					if (aalData != null) {
						Object finalAalObj = aalData.get("final_aal_percentage");
						if (finalAalObj != null) {
							aalScoresMap.put(riskType, ((Number) finalAalObj).intValue());
						}
					}
				}
			}

			candidate.setPhysicalRiskScores(physicalScoresMap);
			candidate.setAalScores(aalScoresMap);
		}

		return candidate;
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
				Double riskScore = scoreObj != null ? ((Number) scoreObj).doubleValue() : 0.0;

				// AAL v11: aal_analysis에서 final_aal_percentage 추출
				Double aal = 0.0;
				Map<String, Object> aalData = (Map<String, Object>) aalAnalysis.get(riskType);
				if (aalData != null) {
					Object finalAalObj = aalData.get("final_aal_percentage");
					if (finalAalObj != null) {
						Double finalAalPercentage = ((Number) finalAalObj).doubleValue();
						// % → 0-100 스케일 유지
						aal = finalAalPercentage;
						log.debug("Risk type: {}, AAL: {}%", riskType, aal);
					}
				}

				RelocationSimulationResponse.RiskData risk = RelocationSimulationResponse.RiskData.builder()
					.riskType(convertRiskTypeName(riskType))
					.physicalRiskScore(riskScore)
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
