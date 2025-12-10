package com.skax.physicalrisk.service.past;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skax.physicalrisk.client.fastapi.FastApiClient;
import com.skax.physicalrisk.dto.response.past.PastDisasterResponse;
import com.skax.physicalrisk.exception.BusinessException;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * 과거 재해 이력 서비스
 *
 * FastAPI 서버를 통한 과거 재해 데이터 조회
 *
 * 최종 수정일: 2025-12-10
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PastDisasterService {

	private final FastApiClient fastApiClient;
	private final ObjectMapper objectMapper;

	/**
	 * 과거 재해 이력 조회
	 *
	 * @param year         연도
	 * @param disasterType 재해 유형
	 * @param severity     심각도
	 * @return 과거 재해 이력
	 */
	public PastDisasterResponse getPastDisasters(int year, String disasterType, String severity) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching past disasters for year={}, disasterType={}, severity={}, userId={}",
			year, disasterType, severity, userId);

		// 파라미터 검증
		if (year < 1900 || year > 2100) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "유효하지 않은 연도입니다");
		}

		// TODO: FastAPI 팀이 getPastDisasters 엔드포인트 구현 후 활성화
		// try {
		// 	// FastAPI로 과거 재해 데이터 요청
		// 	Map<String, Object> response = fastApiClient.getPastDisasters(year, disasterType, severity).block();
		// 	// 응답을 DTO로 변환
		// 	return convertToDto(response, PastDisasterResponse.class);
		// } catch (Exception e) {
		// 	log.error("Failed to fetch past disasters: {}", e.getMessage());
		// 	throw new BusinessException(ErrorCode.FASTAPI_CONNECTION_ERROR,
		// 		"과거 재해 데이터 조회에 실패했습니다: " + e.getMessage());
		// }

		// 임시로 더미 데이터 반환 (FastAPI 엔드포인트 구현 전까지)
		log.info("Returning dummy past disaster data for year={}, disasterType={}, severity={}",
			year, disasterType, severity);

		return PastDisasterResponse.builder()
			.data(PastDisasterResponse.DataWrapper.builder()
				.items(java.util.Collections.emptyList())
				.build())
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
}
