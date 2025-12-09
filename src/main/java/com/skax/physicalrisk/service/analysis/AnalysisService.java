package com.skax.physicalrisk.service.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skax.physicalrisk.client.fastapi.FastApiClient;
import com.skax.physicalrisk.client.fastapi.dto.SiteInfoDto;
import com.skax.physicalrisk.client.fastapi.dto.StartAnalysisRequestDto;
import com.skax.physicalrisk.domain.site.entity.Site;
import com.skax.physicalrisk.domain.site.repository.SiteRepository;
import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.dto.response.analysis.*;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import com.skax.physicalrisk.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 분석 서비스
 *
 * FastAPI 서버를 통한 AI 분석 기능 제공
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
public class AnalysisService {

	private final FastApiClient fastApiClient;
	private final SiteRepository siteRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	/**
	 * 분석 시작
	 *
	 * @param siteId       사업장 ID
	 * @param latitude     위도
	 * @param longitude    경도
	 * @param industryType 산업 유형
	 * @return 작업 상태 응답
	 */
	public AnalysisJobStatusResponse startAnalysis(
		UUID siteId,
		BigDecimal latitude,
		BigDecimal longitude,
		String industryType
	) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Starting analysis for site: {} by user: {}, lat: {}, lon: {}, industry: {}",
			siteId, userId, latitude, longitude, industryType);

		// 사업장 조회 및 권한 확인
		Site site = getSiteWithAuth(siteId, userId);

		// FastAPI 요청 DTO 생성 (사업장 ID, 위경도, 유형)
		SiteInfoDto siteInfo = SiteInfoDto.builder()
			.id(siteId)
			.latitude(latitude)
			.longitude(longitude)
			.industry(industryType)
			.build();

		StartAnalysisRequestDto request = StartAnalysisRequestDto.builder()
			.site(siteInfo)
			.build();

		// WebClient 호출 후 block()으로 동기 변환
		Map<String, Object> response = fastApiClient.startAnalysis(request).block();
		return convertToDto(response, AnalysisJobStatusResponse.class);
	}

	/**
	 * 분석 작업 상태 조회
	 *
	 * @param siteId 사업장 ID
	 * @param jobId  작업 ID
	 * @return 작업 상태
	 */
	public AnalysisJobStatusResponse getAnalysisStatus(UUID siteId, UUID jobId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching analysis status for site: {}, job: {}", siteId, jobId);

		// 권한 확인
		getSiteWithAuth(siteId, userId);
		Map<String, Object> response = fastApiClient.getAnalysisStatus(siteId, jobId).block();
		return convertToDto(response, AnalysisJobStatusResponse.class);
	}

	/**
	 * 대시보드 요약 조회 (전체 사업장)
	 *
	 * @return 대시보드 요약
	 */
	public DashboardSummaryResponse getDashboardSummary() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching dashboard summary for user: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		Map<String, Object> response = fastApiClient.getDashboardSummary(userId).block();
		DashboardSummaryResponse dashboardResponse = convertToDto(response, DashboardSummaryResponse.class);

		// Enrich with coordinates from database
		enrichWithCoordinates(dashboardResponse, user);

		return dashboardResponse;
	}

	/**
	 * 대시보드 응답에 좌표 정보 추가
	 *
	 * @param response 대시보드 응답
	 * @param user 사용자
	 */
	private void enrichWithCoordinates(DashboardSummaryResponse response, User user) {
		if (response.getSites() == null) {
			return;
		}

		// Get all user's sites from database
		List<Site> sites = siteRepository.findByUser(user);
		Map<UUID, Site> siteMap = sites.stream()
			.collect(Collectors.toMap(Site::getId, Function.identity()));

		// Enrich each site summary with coordinates
		response.getSites().forEach(siteSummary -> {
			Site site = siteMap.get(siteSummary.getSiteId());
			if (site != null) {
				siteSummary.setLatitude(site.getLatitude());
				siteSummary.setLongitude(site.getLongitude());
			}
		});
	}

	/**
	 * 물리적 리스크 점수 조회
	 *
	 * @param siteId     사업장 ID
	 * @param hazardType 위험 유형 (옵션)
	 * @return 물리적 리스크 점수
	 */
	public PhysicalRiskScoreResponse getPhysicalRiskScores(UUID siteId, String hazardType) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching physical risk scores for site: {}, hazardType: {}", siteId, hazardType);

		getSiteWithAuth(siteId, userId);
		Map<String, Object> response = fastApiClient.getPhysicalRiskScores(siteId, hazardType).block();
		return convertToDto(response, PhysicalRiskScoreResponse.class);
	}

	/**
	 * 과거 재난 이력 조회
	 *
	 * @param siteId 사업장 ID
	 * @return 과거 이벤트
	 */
	public PastEventsResponse getPastEvents(UUID siteId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching past events for site: {}", siteId);

		getSiteWithAuth(siteId, userId);
		Map<String, Object> response = fastApiClient.getPastEvents(siteId).block();
		return convertToDto(response, PastEventsResponse.class);
	}

	/**
	 * 재무 영향 분석
	 *
	 * @param siteId 사업장 ID
	 * @return 재무 영향
	 */
	public FinancialImpactResponse getFinancialImpact(UUID siteId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching financial impact for site: {}", siteId);

		getSiteWithAuth(siteId, userId);
		Map<String, Object> response = fastApiClient.getFinancialImpact(siteId).block();
		return convertToDto(response, FinancialImpactResponse.class);
	}

	/**
	 * 취약성 분석
	 *
	 * @param siteId 사업장 ID
	 * @return 취약성 분석
	 */
	public VulnerabilityResponse getVulnerability(UUID siteId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching vulnerability for site: {}", siteId);

		getSiteWithAuth(siteId, userId);
		Map<String, Object> response = fastApiClient.getVulnerability(siteId).block();
		return convertToDto(response, VulnerabilityResponse.class);
	}

	/**
	 * 통합 분석 결과
	 *
	 * @param siteId     사업장 ID
	 * @param hazardType 위험 유형
	 * @return 통합 분석 결과
	 */
	public AnalysisTotalResponse getTotalAnalysis(UUID siteId, String hazardType) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching total analysis for site: {}, hazardType: {}", siteId, hazardType);

		getSiteWithAuth(siteId, userId);
		Map<String, Object> response = fastApiClient.getTotalAnalysis(siteId, hazardType).block();
		return convertToDto(response, AnalysisTotalResponse.class);
	}

	/**
	 * 사업장 조회 및 권한 확인
	 *
	 * @param siteId 사업장 ID
	 * @param userId 사용자 ID
	 * @return 사업장 엔티티
	 */
	private Site getSiteWithAuth(UUID siteId, UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		return siteRepository.findByIdAndUser(siteId, user)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND));
	}

	/**
	 * Map을 DTO로 변환
	 *
	 * @param map   소스 맵
	 * @param clazz 대상 DTO 클래스
	 * @return 변환된 DTO
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
