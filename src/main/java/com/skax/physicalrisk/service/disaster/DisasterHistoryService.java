package com.skax.physicalrisk.service.disaster;

import com.skax.physicalrisk.client.fastapi.FastApiClient;
import com.skax.physicalrisk.domain.disaster.entity.DisasterSeverity;
import com.skax.physicalrisk.domain.disaster.entity.DisasterType;
import com.skax.physicalrisk.dto.response.disaster.DisasterHistoryItem;
import com.skax.physicalrisk.dto.response.disaster.DisasterHistoryListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 재해 이력 서비스
 *
 * FastAPI 재해 이력 API 프록시
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisasterHistoryService {

	private final FastApiClient fastApiClient;

	/**
	 * 재해 이력 목록 조회
	 *
	 * @param adminCode 행정구역 코드 (옵션)
	 * @param year 연도 (옵션)
	 * @param disasterType 재해 유형 (옵션)
	 * @param page 페이지 번호 (1부터 시작)
	 * @param pageSize 페이지당 개수
	 * @return 재해 이력 목록 응답
	 */
	public Mono<DisasterHistoryListResponse> getDisasterHistory(
		String adminCode,
		Integer year,
		String disasterType,
		Integer page,
		Integer pageSize
	) {
		log.info("재해 이력 조회: adminCode={}, year={}, disasterType={}, page={}, pageSize={}",
			adminCode, year, disasterType, page, pageSize);

		// 기본값 설정
		int actualPage = (page != null && page > 0) ? page : 1;
		int actualPageSize = (pageSize != null && pageSize > 0) ? pageSize : 20;

		return fastApiClient.getDisasterHistory(adminCode, year, disasterType, actualPage, actualPageSize)
			.map(response -> {
				// items 배열 파싱
				List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) response.get("items");
				List<DisasterHistoryItem> items = itemMaps.stream()
					.map(this::mapToDisasterHistoryItem)
					.collect(Collectors.toList());

				// 응답 생성
				return DisasterHistoryListResponse.builder()
					.items(items)
					.total(((Number) response.get("total")).intValue())
					.page(((Number) response.get("page")).intValue())
					.pageSize(((Number) response.get("pageSize")).intValue())
					.build();
			});
	}

	/**
	 * Map을 DisasterHistoryItem으로 변환
	 *
	 * @param map FastAPI 응답 Map
	 * @return DisasterHistoryItem
	 */
	private DisasterHistoryItem mapToDisasterHistoryItem(Map<String, Object> map) {
		return DisasterHistoryItem.builder()
			.yearbookId(((Number) map.get("yearbookId")).intValue())
			.year(((Number) map.get("year")).intValue())
			.adminCode((String) map.get("adminCode"))
			.disasterType(map.get("disasterType") != null ?
				DisasterType.fromCode(map.get("disasterType").toString()) : null)
			.totalDamage(map.get("totalDamage") != null ?
				((Number) map.get("totalDamage")).doubleValue() : null)
			.damageLevel(map.get("damageLevel") != null ?
				DisasterSeverity.fromCode(map.get("damageLevel").toString()) : null)
			.affectedBuildings(map.get("affectedBuildings") != null ?
				((Number) map.get("affectedBuildings")).intValue() : null)
			.affectedPopulation(map.get("affectedPopulation") != null ?
				((Number) map.get("affectedPopulation")).intValue() : null)
			.build();
	}
}
