package com.skax.physicalrisk.service.report;

import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import com.skax.physicalrisk.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 리포트 서비스
 *
 * FastAPI 서버를 통한 리포트 생성 및 조회
 *
 * 최종 수정일: 2025-11-20
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

	private final UserRepository userRepository;

	/**
	 * 통합 리포트 조회
	 *
	 * @return 통합 리포트 내용 (ceosummry, Governance, strategy, riskmanagement, goal)
	 */
	public Map<String, String> getReport() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching report for userId={}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// TODO: FastAPI 팀이 getReport 엔드포인트 구현 후 활성화
		// try {
		// 	Map<String, Object> response = fastApiClient.getReportByUserId(userId).block();
		// 	return Map.of(
		// 		"ceosummry", (String) response.get("ceosummry"),
		// 		"Governance", (String) response.get("Governance"),
		// 		"strategy", (String) response.get("strategy"),
		// 		"riskmanagement", (String) response.get("riskmanagement"),
		// 		"goal", (String) response.get("goal")
		// 	);
		// } catch (Exception e) {
		// 	log.error("Failed to fetch report: {}", e.getMessage());
		// 	throw new BusinessException(ErrorCode.FASTAPI_CONNECTION_ERROR,
		// 		"리포트 조회에 실패했습니다: " + e.getMessage());
		// }

		// 임시로 더미 데이터 반환 (FastAPI 엔드포인트 구현 전까지)
		log.info("Returning dummy report data for userId={}", userId);
		return Map.of(
			"ceosummry", "회사는 현재 기후 관련 위험을 면밀히 분석했습니다",
			"Governance", "기후 거버넌스는 당사의 지속 가능한 운영과 자산 가치를 극대화하기 위한 필수 요소입니다.",
			"strategy", "기후 변화에 대한 포괄적 접근 방식을 통해 우리는 지속 가능한 운영을 도모합니다.",
			"riskmanagement", "리스크 관리의 일환으로 당사는 여러 프로세스를 도입했습니다.",
			"goal", "현재 기후 리스크로 인해 예상되는 손실과 당사의 목표입니다."
		);
	}

	/**
	 * 리포트 추가 데이터 등록 (v0.2 신규)
	 *
	 * @param request 리포트 추가 데이터 요청 (siteId, data)
	 */
	@Transactional
	public void registerReportData(Map<String, Object> request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Registering report data for user: {}, request: {}", userId, request);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// FastAPI로 리포트 데이터 등록 요청
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("userId", userId);
		requestMap.putAll(request);

		// TODO: FastAPI 팀이 registerReportData 엔드포인트 구현 후 활성화
		// 비동기 처리 (FastAPI가 해당 엔드포인트를 지원한다고 가정)
		// try {
		// 	fastApiClient.registerReportData(requestMap).block();
		// 	log.info("Report data registered successfully for userId={}", userId);
		// } catch (Exception e) {
		// 	log.error("Failed to register report data for userId={}: {}", userId, e.getMessage());
		// }

		// 임시로 로컬에 저장 (FastAPI 엔드포인트 구현 전까지)
		log.info("Report data stored locally for userId={}: {}", userId, requestMap);
	}
}
