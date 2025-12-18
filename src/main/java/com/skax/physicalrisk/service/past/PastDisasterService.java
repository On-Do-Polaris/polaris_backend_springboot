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
	 * @param year         연도 (optional)
	 * @param disasterType 재해 유형 (optional)
	 * @param severity     심각도 (optional)
	 * @return 과거 재해 이력
	 */
	public PastDisasterResponse getPastDisasters(Integer year, String disasterType, String severity) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching past disasters for year={}, disasterType={}, severity={}, userId={}",
			year, disasterType, severity, userId);

		// 파라미터 검증 (year가 제공된 경우에만)
		if (year != null && (year < 1900 || year > 2100)) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "유효하지 않은 연도입니다");
		}

		try {
			// FastAPI로 과거 재해 데이터 요청
			Map<String, Object> response = fastApiClient.getPastDisasters(year, disasterType, severity).block();

			// 응답을 DTO로 변환
			PastDisasterResponse pastDisasterResponse = convertToDto(response, PastDisasterResponse.class);

			// FastAPI가 필터링을 제대로 하지 못하는 경우를 대비해 Java에서 추가 필터링
			return filterDisasters(pastDisasterResponse, year, disasterType, severity);
		} catch (Exception e) {
			log.error("Failed to fetch past disasters: {}", e.getMessage());
			throw new BusinessException(ErrorCode.FASTAPI_CONNECTION_ERROR,
				"과거 재해 데이터 조회에 실패했습니다: " + e.getMessage());
		}
	}

	/**
	 * 재해 이력 필터링
	 *
	 * @param response     FastAPI 응답
	 * @param year         연도 필터
	 * @param disasterType 재해 유형 필터
	 * @param severity     심각도 필터
	 * @return 필터링된 재해 이력
	 */
	private PastDisasterResponse filterDisasters(PastDisasterResponse response,
												  Integer year,
												  String disasterType,
												  String severity) {
		if (response == null || response.getData() == null) {
			return response;
		}

		// 필터링 조건이 없으면 그대로 반환
		if (year == null && disasterType == null && severity == null) {
			log.info("No filter criteria provided, returning all disasters");
			return response;
		}

		// 필터링 수행
		var filteredItems = response.getData().stream()
			.filter(item -> {
				// year 필터링 (alertDate에서 연도 추출)
				if (year != null && item.getAlertDate() != null) {
					String itemYear = item.getAlertDate().substring(0, 4); // "2023-07-15" -> "2023"
					if (!String.valueOf(year).equals(itemYear)) {
						return false;
					}
				}

				// disasterType 필터링
				if (disasterType != null && !disasterType.isEmpty()) {
					if (item.getDisasterType() == null || !item.getDisasterType().equals(disasterType)) {
						return false;
					}
				}

				// severity 필터링
				if (severity != null && !severity.isEmpty()) {
					if (item.getSeverity() == null || !item.getSeverity().equals(severity)) {
						return false;
					}
				}

				return true;
			})
			.toList();

		log.info("Filtered disasters: original={}, filtered={}, year={}, disasterType={}, severity={}",
			response.getData().size(), filteredItems.size(), year, disasterType, severity);

		return PastDisasterResponse.builder()
			.data(filteredItems)
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
