package com.skax.physicalrisk.service.report;

import com.skax.physicalrisk.client.fastapi.FastApiClient;
import com.skax.physicalrisk.domain.report.entity.Report;
import com.skax.physicalrisk.domain.report.repository.ReportRepository;
import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.exception.BusinessException;
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
 * 최종 수정일: 2025-12-14
 * 파일 버전: v02 - Report 테이블 저장 로직 추가
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

	private final FastApiClient fastApiClient;
	private final UserRepository userRepository;
	private final ReportRepository reportRepository;

	/**
	 * 통합 리포트 조회 및 저장
	 *
	 * FastAPI로부터 리포트를 조회하고 Report 테이블에 저장
	 *
	 * @return 통합 리포트 내용 (ceosummry, Governance, strategy, riskmanagement, goal)
	 */
	@Transactional
	public Map<String, String> getReport() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching report for userId={}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		try {
			// FastAPI에서 리포트 조회
			Map<String, Object> response = fastApiClient.getReportByUserId(userId).block();

			// Report 엔티티 생성 또는 업데이트
			Report report = reportRepository.findByUser(user)
				.orElse(Report.builder()
					.user(user)
					.build());

			// JSONB로 전체 응답 저장
			report.setReportContent(response);

			// DB에 저장
			reportRepository.save(report);
			log.info("Report saved successfully for userId={}, reportId={}", userId, report.getId());

			// 응답 반환 (기존과 동일한 5개 필드)
			return Map.of(
				"ceosummry", (String) response.get("ceosummry"),
				"Governance", (String) response.get("Governance"),
				"strategy", (String) response.get("strategy"),
				"riskmanagement", (String) response.get("riskmanagement"),
				"goal", (String) response.get("goal")
			);
		} catch (Exception e) {
			log.error("Failed to fetch report: {}", e.getMessage());
			throw new BusinessException(ErrorCode.FASTAPI_CONNECTION_ERROR,
				"리포트 조회에 실패했습니다: " + e.getMessage());
		}
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

		try {
			fastApiClient.registerReportData(requestMap).block();
			log.info("Report data registered successfully for userId={}", userId);
		} catch (Exception e) {
			log.error("Failed to register report data for userId={}: {}", userId, e.getMessage());
			throw new BusinessException(ErrorCode.FASTAPI_CONNECTION_ERROR,
				"리포트 데이터 등록에 실패했습니다: " + e.getMessage());
		}
	}
}
