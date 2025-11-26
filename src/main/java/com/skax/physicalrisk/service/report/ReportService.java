package com.skax.physicalrisk.service.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skax.physicalrisk.client.fastapi.FastApiClient;
import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.dto.request.report.CreateReportRequest;
import com.skax.physicalrisk.dto.response.report.ReportPdfResponse;
import com.skax.physicalrisk.dto.response.report.ReportWebViewResponse;
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

	private final FastApiClient fastApiClient;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	/**
	 * 리포트 생성
	 *
	 * @param request 리포트 생성 요청
	 * @return 생성된 리포트 ID
	 */
	@Transactional
	public Map<String, Object> createReport(CreateReportRequest request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Creating report for user: {}, siteId: {}", userId, request.getSiteId());

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// FastAPI로 리포트 생성 요청
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("userId", userId);
		requestMap.put("siteId", request.getSiteId());

		Map<String, Object> response = fastApiClient.createReport(requestMap).block();

		log.info("Report created successfully: reportId={}", response.get("reportId"));
		return response;
	}

	/**
	 * 리포트 웹 뷰 조회
	 *
	 * @return 웹 뷰 리포트 (이미지 페이지들)
	 */
	public ReportWebViewResponse getReportWebView() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching web view report for userId={}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		Map<String, Object> response = fastApiClient.getReportWebViewByUserId(userId).block();
		return convertToDto(response, ReportWebViewResponse.class);
	}

	/**
	 * 리포트 PDF 다운로드 정보 조회
	 *
	 * @return PDF 다운로드 정보
	 */
	public ReportPdfResponse getReportPdf() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching PDF report for userId={}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		Map<String, Object> response = fastApiClient.getReportPdfByUserId(userId).block();
		return convertToDto(response, ReportPdfResponse.class);
	}

	/**
	 * 리포트 삭제
	 */
	@Transactional
	public void deleteReport() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Deleting report for userId={}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		fastApiClient.deleteReportByUserId(userId).block();
		log.info("Report deleted successfully for userId={}", userId);
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
