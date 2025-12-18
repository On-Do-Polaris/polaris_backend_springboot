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
    private final com.skax.physicalrisk.service.user.EmailService emailService;

    /**
     * 분석 시작 (단일 사업장)
     *
     * @param siteId        사업장 ID
     * @param latitude      위도
     * @param longitude     경도
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

        // FastAPI 요청 DTO 생성 - SiteInfoDto.from()을 사용하여 모든 필드와 매핑 로직 적용
        SiteInfoDto siteInfo = SiteInfoDto.from(site);

        // 단일 사업장도 리스트로 전송 (sites 필드 사용)
        StartAnalysisRequestDto request = StartAnalysisRequestDto.builder()
            .userId(userId)            // 로그인한 사용자 ID
            .sites(List.of(siteInfo))  // 단일 사업장을 리스트로 감싸서 전송
            .hazardTypes(List.of())    // 빈 리스트로 초기화
            .priority("normal")        // 기본 우선순위
            .build();

        // WebClient 호출 후 block()으로 동기 변환
        Map<String, Object> response = fastApiClient.startAnalysis(request).block();
        return convertToDto(response, AnalysisJobStatusResponse.class);
    }

    /**
     * 분석 작업 상태 조회 (v0.2: jobId 제거, status만 반환)
     *
     * @param jobid  작업 ID (선택, 사용하지 않음)
     * @return 작업 상태 (ing 또는 done)
     */
    public AnalysisJobStatusResponse getAnalysisStatus(UUID jobid) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Fetching analysis status for userId: {}", userId);

        // 사용자 인증 확인
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // userId를 FastAPI로 전달
        Map<String, Object> response = fastApiClient.getAnalysisStatus(userId, jobid).block();

        // FastAPI 응답에서 status 추출 및 변환
        String fastApiStatus = (String) response.getOrDefault("status", "unknown");
        String simplifiedStatus = convertToSimplifiedStatus(fastApiStatus);

        log.info("Analysis status for userId {}: FastAPI={}, Simplified={}", userId, fastApiStatus, simplifiedStatus);

        return AnalysisJobStatusResponse.builder()
            .status(simplifiedStatus)
            .build();
    }

    /**
     * FastAPI 상태를 간소화된 상태로 변환
     *
     * @param fastApiStatus FastAPI에서 받은 상태
     * @return "ing" 또는 "done"
     */
    private String convertToSimplifiedStatus(String fastApiStatus) {
        if (fastApiStatus == null) {
            return "ing";
        }

        // FastAPI 상태값: queued, running, processing, completed, failed, done 등
        return switch (fastApiStatus.toLowerCase()) {
            case "completed", "done", "finished", "success" -> "done";
            case "queued", "running", "processing", "pending", "in_progress" -> "ing";
            default -> "ing"; // 기본값은 진행 중으로 처리
        };
    }

    /**
     * 분석 개요 조회 (v0.2)
     *
     * @param siteId 사업장 ID
     * @return 분석 개요 정보
     */
    public Map<String, Object> getAnalysisSummary(UUID siteId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Fetching analysis summary for site: {}, user: {}", siteId, userId);

        // 권한 확인 및 Site 정보 조회
        Site site = getSiteWithAuth(siteId, userId);

        // Site에서 위경도 추출
        Double latitude = site.getLatitude() != null ? site.getLatitude().doubleValue() : null;
        Double longitude = site.getLongitude() != null ? site.getLongitude().doubleValue() : null;

        log.info("Calling FastAPI with siteId={}, latitude={}, longitude={}", siteId, latitude, longitude);

        Map<String, Object> response = fastApiClient.getAnalysisSummary(siteId, latitude, longitude).block();
        return response;
    }

    /**
     * 대시보드 요약 조회 (전체 사업장)
     *
     * @deprecated DashboardService.getDashboardSummary() 사용
     * @return 대시보드 요약
     */
    @Deprecated
    public DashboardSummaryResponse getDashboardSummary() {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Fetching dashboard summary for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // Find all sites for the user
        List<Site> userSites = siteRepository.findByUser(user);
        List<UUID> siteIds = userSites.stream().map(Site::getId).collect(Collectors.toList());

        if (siteIds.isEmpty()) {
            log.warn("No sites found for user {}, returning empty dashboard summary.", userId);
            return DashboardSummaryResponse.builder()
                .mainClimateRisk("데이터 없음")
                .sites(List.of())
                .build();
        }

        Map<String, Object> response = fastApiClient.getDashboardSummary(siteIds).block();
        DashboardSummaryResponse dashboardResponse = convertToDto(response, DashboardSummaryResponse.class);

        // Enrich with coordinates from database
        enrichWithCoordinates(dashboardResponse, user);

        return dashboardResponse;
    }

    /**
     * 대시보드 응답에 좌표 정보 및 건물 정보 추가
     * (SiteService처럼 DB에서 직접 가져오는 방식)
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

        // Enrich each site summary with coordinates and building info from DB
        response.getSites().forEach(siteSummary -> {
            Site site = siteMap.get(siteSummary.getSiteId());
            if (site != null) {
                // 좌표 정보
                siteSummary.setLatitude(site.getLatitude());
                siteSummary.setLongitude(site.getLongitude());

                // 주소 및 이름 정보 (DB에서 직접 가져오기)
                siteSummary.setJibunAddress(site.getJibunAddress());
                siteSummary.setRoadAddress(site.getRoadAddress());
                siteSummary.setSiteName(site.getName());
                siteSummary.setSiteType(site.getType());

                // 건물 정보 추가 (내부 로직용, 응답 스키마에는 노출 안됨)
                siteSummary.setBuildingAge(site.getBuildingAge());
                siteSummary.setBuildingType(site.getBuildingType());
                siteSummary.setSeismicDesign(site.getSeismicDesign());
                siteSummary.setGrossFloorArea(site.getGrossFloorArea());
            }
        });
    }

    /**
     * 물리적 리스크 점수 조회 (v0.2: /api/analysis/physical-risk)
     *
     * @param siteId    사업장 ID
     * @param hazardType 위험 유형 (옵션)
     * @return 물리적 리스크 점수
     */
    public PhysicalRiskScoreResponse getPhysicalRiskScores(UUID siteId, String hazardType, String term) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Fetching physical risk scores for site: {}, hazardType: {}, term: {}", siteId, hazardType, term);

        getSiteWithAuth(siteId, userId);
        Map<String, Object> response = fastApiClient.getPhysicalRiskScores(siteId, hazardType, term).block();

        log.debug("FastAPI physical-risk-scores response: {}", response);

        // FastAPI 응답을 DTO로 자동 매핑 (camelCase 지원)
        PhysicalRiskScoreResponse.FastApiResponse fastApiResponse =
            objectMapper.convertValue(response, PhysicalRiskScoreResponse.FastApiResponse.class);

        if (fastApiResponse.getScenarios() == null || fastApiResponse.getScenarios().isEmpty()) {
            log.warn("No scenarios found in FastAPI response for siteId: {}", siteId);
            return PhysicalRiskScoreResponse.builder()
                .siteId(siteId)
                .term(term)
                .hazardType(hazardType)
                .build();
        }

        // 시나리오를 SSP1-2.6, SSP2-4.5, SSP3-7.0, SSP5-8.5로 분류
        Map<String, PhysicalRiskScoreResponse.RiskScoreDetail> scenarios1 = null;
        Map<String, PhysicalRiskScoreResponse.RiskScoreDetail> scenarios2 = null;
        Map<String, PhysicalRiskScoreResponse.RiskScoreDetail> scenarios3 = null;
        Map<String, PhysicalRiskScoreResponse.RiskScoreDetail> scenarios4 = null;

        for (PhysicalRiskScoreResponse.ScenarioData scenario : fastApiResponse.getScenarios()) {
            String scenarioName = scenario.getScenario();
            String riskType = scenario.getRiskType();

            log.debug("Processing scenario: {}, riskType: {}, requested hazardType: {}", scenarioName, riskType, hazardType);

            // hazardType 필터링 (선택사항)
            if (hazardType != null && !hazardType.isEmpty() && !hazardType.equals(riskType)) {
                log.debug("Skipping scenario due to riskType mismatch: {} != {}", hazardType, riskType);
                continue;
            }

            // term에 따라 해당 데이터 추출
            Map<String, PhysicalRiskScoreResponse.RiskScoreDetail> termData = null;
            switch (term) {
                case "short":
                    termData = scenario.getShortTerm();
                    break;
                case "mid":
                    termData = scenario.getMidTerm();
                    break;
                case "long":
                    termData = scenario.getLongTerm();
                    break;
                default:
                    log.warn("Unknown term: {}", term);
                    continue;
            }

            if (termData == null) {
                log.debug("No data found for term: {}", term);
                continue;
            }

            log.debug("Found termData for {}: {} points", term, termData.size());

            // 시나리오별로 분류
            switch (scenarioName) {
                case "SSP1-2.6":
                    scenarios1 = termData;
                    break;
                case "SSP2-4.5":
                    scenarios2 = termData;
                    break;
                case "SSP3-7.0":
                    scenarios3 = termData;
                    break;
                case "SSP5-8.5":
                    scenarios4 = termData;
                    break;
                default:
                    log.warn("Unknown scenario: {}", scenarioName);
            }
        }

        PhysicalRiskScoreResponse result = PhysicalRiskScoreResponse.builder()
            .siteId(siteId)
            .term(term)
            .hazardType(hazardType)
            .scenarios1(scenarios1)
            .scenarios2(scenarios2)
            .scenarios3(scenarios3)
            .scenarios4(scenarios4)
            .Strategy(fastApiResponse.getStrategy())
            .build();

        log.debug("Converted PhysicalRiskScoreResponse: {}", result);
        return result;
    }

    /**
     * 재무 영향 분석 (v0.2: /api/analysis/aal)
     *
     * @param siteId 사업장 ID
     * @return 재무 영향
     */
    public FinancialImpactResponse getFinancialImpact(UUID siteId, String hazardType, String term) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Fetching financial impact for site: {}, hazardType: {}, term: {}", siteId, hazardType, term);

        getSiteWithAuth(siteId, userId);
        Map<String, Object> response = fastApiClient.getFinancialImpact(siteId, hazardType, term).block();

        log.debug("FastAPI AAL response: {}", response);

        // FastAPI 응답을 DTO로 자동 매핑 (camelCase 지원)
        FinancialImpactResponse.FastApiResponse fastApiResponse =
            objectMapper.convertValue(response, FinancialImpactResponse.FastApiResponse.class);

        if (fastApiResponse.getScenarios() == null || fastApiResponse.getScenarios().isEmpty()) {
            log.warn("No scenarios found in FastAPI response for siteId: {}", siteId);
            return FinancialImpactResponse.builder()
                .siteId(siteId)
                .term(term)
                .hazardType(hazardType)
                .build();
        }

        // 시나리오를 SSP1-2.6, SSP2-4.5, SSP3-7.0, SSP5-8.5로 분류
        Map<String, Integer> scenarios1 = null;
        Map<String, Integer> scenarios2 = null;
        Map<String, Integer> scenarios3 = null;
        Map<String, Integer> scenarios4 = null;

        for (FinancialImpactResponse.ScenarioData scenario : fastApiResponse.getScenarios()) {
            String scenarioName = scenario.getScenario();
            String riskType = scenario.getRiskType();

            log.debug("Processing financial scenario: {}, riskType: {}, requested hazardType: {}", scenarioName, riskType, hazardType);

            // hazardType 필터링 (선택사항)
            if (hazardType != null && !hazardType.isEmpty() && !hazardType.equals(riskType)) {
                log.debug("Skipping financial scenario due to riskType mismatch: {} != {}", hazardType, riskType);
                continue;
            }

            // term에 따라 해당 데이터 추출
            Map<String, Integer> termData = null;
            switch (term) {
                case "short":
                    termData = scenario.getShortTerm();
                    break;
                case "mid":
                    termData = scenario.getMidTerm();
                    break;
                case "long":
                    termData = scenario.getLongTerm();
                    break;
                default:
                    log.warn("Unknown term: {}", term);
                    continue;
            }

            if (termData == null) {
                log.debug("No financial data found for term: {}", term);
                continue;
            }

            log.debug("Found financial termData for {}: {} points", term, termData.size());

            // 시나리오별로 분류
            switch (scenarioName) {
                case "SSP1-2.6":
                    scenarios1 = termData;
                    break;
                case "SSP2-4.5":
                    scenarios2 = termData;
                    break;
                case "SSP3-7.0":
                    scenarios3 = termData;
                    break;
                case "SSP5-8.5":
                    scenarios4 = termData;
                    break;
                default:
                    log.warn("Unknown scenario: {}", scenarioName);
            }
        }

        FinancialImpactResponse result = FinancialImpactResponse.builder()
            .siteId(siteId)
            .term(term)
            .hazardType(hazardType)
            .scenarios1(scenarios1)
            .scenarios2(scenarios2)
            .scenarios3(scenarios3)
            .scenarios4(scenarios4)
            .reason(fastApiResponse.getReason())
            .build();

        log.debug("Converted FinancialImpactResponse: {}", result);
        return result;
    }

    /**
     * 취약성 분석 (v0.2: /api/analysis/vulnerability)
     *
     * @param siteId 사업장 ID
     * @return 취약성 분석
     */
    @SuppressWarnings("unchecked")
    public VulnerabilityResponse getVulnerability(UUID siteId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Fetching vulnerability for site: {}", siteId);

        Site site = getSiteWithAuth(siteId, userId);
        Map<String, Object> response = fastApiClient.getVulnerability(siteId).block();

        log.debug("FastAPI vulnerability response: {}", response);

        // FastAPI 응답에서 data 객체 추출
        Map<String, Object> data = response.get("data") != null
            ? (Map<String, Object>) response.get("data")
            : response;

        // FastAPI 응답을 DTO로 변환 (기본 사업장 정보 + FastAPI data)
        VulnerabilityResponse result = VulnerabilityResponse.builder()
            .siteId(site.getId())
            .siteName(site.getName())
            .latitude(site.getLatitude())
            .longitude(site.getLongitude())
            .jibunAddress(site.getJibunAddress())
            .roadAddress(site.getRoadAddress())
            .siteType(site.getType())
            // FastAPI data에서 건물 정보 추출
            .area(data.get("area") != null ? ((Number) data.get("area")).doubleValue() : null)
            .grndflrCnt(data.get("grndflrCnt") != null ? ((Number) data.get("grndflrCnt")).intValue() : null)
            .ugrnFlrCnt(data.get("ugrnFlrCnt") != null ? ((Number) data.get("ugrnFlrCnt")).intValue() : null)
            .rserthqkDsgnApplyYn(data.get("rserthqkDsgnApplyYn") != null ? data.get("rserthqkDsgnApplyYn").toString() : null)
            .aisummry(data.get("aisummry") != null ? data.get("aisummry").toString() : null)
            .build();

        log.debug("Vulnerability response for site {}: {}", siteId, result);
        return result;
    }

    /**
     * 다중 사업장 분석 시작 (v0.2 - 단일 FastAPI 호출)
     *
     * @param sites 사업장 ID 목록
     */
    public void startAnalysisMultiple(List<com.skax.physicalrisk.controller.AnalysisController.StartAnalysisRequest.SiteIdWrapper> sites) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Starting analysis for {} sites by user: {}", sites.size(), userId);

        // 모든 사업장 정보를 SiteInfoDto 리스트로 변환
        List<SiteInfoDto> siteInfoList = sites.stream()
            .map(siteWrapper -> {
                UUID siteId = siteWrapper.getSiteId();
                try {
                    // 사업장 조회 및 권한 확인
                    Site site = getSiteWithAuth(siteId, userId);
                    return SiteInfoDto.from(site);
                } catch (Exception e) {
                    log.error("Error loading site {}: {}", siteId, e.getMessage());
                    return null;
                }
            })
            .filter(siteInfo -> siteInfo != null)  // 실패한 사업장 제외
            .collect(Collectors.toList());

        if (siteInfoList.isEmpty()) {
            log.warn("No valid sites to analyze");
            return;
        }

        // 단일 FastAPI 요청으로 모든 사업장 분석 시작
        StartAnalysisRequestDto request = StartAnalysisRequestDto.builder()
            .userId(userId)          // 로그인한 사용자 ID
            .sites(siteInfoList)
            .hazardTypes(List.of())  // 빈 리스트로 초기화
            .priority("normal")      // 기본 우선순위
            .build();

        // FastAPI 호출 (비동기)
        fastApiClient.startAnalysis(request)
            .doOnSuccess(response -> log.info("Analysis started for {} sites", siteInfoList.size()))
            .doOnError(error -> log.error("Failed to start analysis for sites", error))
            .subscribe();

        log.info("Analysis start request sent for all sites");
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
     * 분석 완료 알림 (FastAPI 콜백용)
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void notifyAnalysisCompletion(UUID userId) {
        log.info("Notifying analysis completion for user: {}", userId);

        // 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 완료 이메일 발송
        emailService.sendAnalysisCompletionEmail(user.getEmail());

        log.info("Analysis completion notification sent to: {}", user.getEmail());
    }

    /**
     * Map을 DTO로 변환
     *
     * @param map    소스 맵
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