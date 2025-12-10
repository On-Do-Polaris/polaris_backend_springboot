package com.skax.physicalrisk.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HazardType 매퍼
 *
 * Spring Boot의 영문 HazardType을 FastAPI가 기대하는 한글 값으로 변환
 *
 * 최종 수정일: 2025-12-10
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
public class HazardTypeMapper {

	private static final Map<String, String> HAZARD_TYPE_MAP = Map.of(
		"TYPHOON", "태풍",
		"INLAND_FLOOD", "내륙침수",
		"COASTAL_FLOOD", "해안침수",
		"URBAN_FLOOD", "도시침수",
		"DROUGHT", "가뭄",
		"WILDFIRE", "산불",
		"HIGH_TEMPERATURE", "폭염",
		"COLD_WAVE", "한파",
		"WATER_SCARCITY", "물부족"
	);

	/**
	 * Spring Boot HazardType을 FastAPI 한글 값으로 변환
	 *
	 * @param springHazardType Spring Boot에서 사용하는 영문 HazardType
	 * @return FastAPI가 기대하는 한글 값
	 * @throws IllegalArgumentException 알 수 없는 HazardType인 경우
	 */
	public static String toFastApiValue(String springHazardType) {
		if (springHazardType == null) {
			throw new IllegalArgumentException("HazardType cannot be null");
		}

		// 이미 한글이면 그대로 반환
		if (HAZARD_TYPE_MAP.containsValue(springHazardType)) {
			return springHazardType;
		}

		// 영문을 한글로 변환
		String mapped = HAZARD_TYPE_MAP.get(springHazardType.toUpperCase().trim());
		if (mapped == null) {
			throw new IllegalArgumentException(
				"Unknown hazard type: " + springHazardType +
				". Valid values: " + HAZARD_TYPE_MAP.keySet()
			);
		}
		return mapped;
	}

	/**
	 * Spring Boot HazardType 리스트를 FastAPI 한글 값 리스트로 변환
	 *
	 * @param springHazardTypes Spring Boot에서 사용하는 영문 HazardType 리스트
	 * @return FastAPI가 기대하는 한글 값 리스트
	 */
	public static List<String> toFastApiValues(List<String> springHazardTypes) {
		if (springHazardTypes == null) {
			return null;
		}

		return springHazardTypes.stream()
			.map(HazardTypeMapper::toFastApiValue)
			.collect(Collectors.toList());
	}
}
