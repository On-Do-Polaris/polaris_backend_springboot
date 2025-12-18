package com.skax.physicalrisk.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HazardType 매퍼
 *
 * Spring Boot의 영문 HazardType을 FastAPI가 기대하는 한글 값으로 변환
 * 역방향 매핑도 지원 (FastAPI 한글 → Spring Boot 표준 한글)
 *
 * 최종 수정일: 2025-12-19
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
public class HazardTypeMapper {

	// Spring Boot 영문 → FastAPI 한글
	private static final Map<String, String> ENGLISH_TO_FASTAPI = Map.of(
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

	// Spring Boot 표준 한글명 (사용자에게 보여지는 이름)
	private static final Map<String, String> STANDARD_KOREAN_NAMES = Map.of(
		"태풍", "태풍",
		"내륙침수", "하천 홍수",
		"해안침수", "해수면 상승",
		"도시침수", "도시 홍수",
		"가뭄", "가뭄",
		"산불", "산불",
		"폭염", "극심한 고온",
		"한파", "극심한 저온",
		"물부족", "물 부족"
	);

	// FastAPI 한글 → Spring Boot 표준 한글 (역매핑)
	private static final Map<String, String> FASTAPI_TO_STANDARD;

	static {
		FASTAPI_TO_STANDARD = new HashMap<>();
		FASTAPI_TO_STANDARD.put("태풍", "태풍");
		FASTAPI_TO_STANDARD.put("내륙침수", "하천 홍수");  // DB에 "하천 홍수"로 저장됨
		FASTAPI_TO_STANDARD.put("해안침수", "해수면 상승");
		FASTAPI_TO_STANDARD.put("도시침수", "도시 홍수");  // DB에 "도시 홍수"로 저장됨
		FASTAPI_TO_STANDARD.put("도시 침수", "도시 홍수");  // FastAPI에서 "도시 침수"로 올 경우도 처리
		FASTAPI_TO_STANDARD.put("가뭄", "가뭄");
		FASTAPI_TO_STANDARD.put("산불", "산불");
		FASTAPI_TO_STANDARD.put("폭염", "극심한 고온");
		FASTAPI_TO_STANDARD.put("한파", "극심한 저온");
		FASTAPI_TO_STANDARD.put("물부족", "물 부족");

		// 역방향도 지원 (표준 한글 → 표준 한글)
		FASTAPI_TO_STANDARD.put("극심한 고온", "극심한 고온");
		FASTAPI_TO_STANDARD.put("극심한 저온", "극심한 저온");
		FASTAPI_TO_STANDARD.put("홍수", "홍수");
		FASTAPI_TO_STANDARD.put("하천 홍수", "하천 홍수");  // DB 저장값
		FASTAPI_TO_STANDARD.put("도시 홍수", "도시 홍수");  // DB 저장값
		FASTAPI_TO_STANDARD.put("해수면 상승", "해수면 상승");
		FASTAPI_TO_STANDARD.put("물 부족", "물 부족");
	}

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

		// 이미 FastAPI 한글이면 그대로 반환
		if (ENGLISH_TO_FASTAPI.containsValue(springHazardType)) {
			return springHazardType;
		}

		// 영문을 한글로 변환
		String mapped = ENGLISH_TO_FASTAPI.get(springHazardType.toUpperCase().trim());
		if (mapped == null) {
			throw new IllegalArgumentException(
				"Unknown hazard type: " + springHazardType +
				". Valid values: " + ENGLISH_TO_FASTAPI.keySet()
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

	/**
	 * FastAPI 한글 riskType을 Spring Boot 표준 한글 hazardType으로 변환
	 *
	 * 예: "폭염" → "극심한 고온", "한파" → "극심한 저온"
	 *
	 * @param fastApiRiskType FastAPI에서 반환하는 한글 riskType
	 * @return Spring Boot 표준 한글 hazardType
	 */
	public static String toStandardKorean(String fastApiRiskType) {
		if (fastApiRiskType == null) {
			return null;
		}

		// 매핑 테이블에서 변환
		String standardName = FASTAPI_TO_STANDARD.get(fastApiRiskType);

		// 매핑이 없으면 원본 그대로 반환
		return standardName != null ? standardName : fastApiRiskType;
	}

	/**
	 * Spring Boot 표준 한글 hazardType을 FastAPI 한글 riskType으로 변환
	 *
	 * 예: "극심한 고온" → "폭염", "극심한 저온" → "한파"
	 *
	 * @param standardKorean Spring Boot 표준 한글 hazardType
	 * @return FastAPI 한글 riskType
	 */
	public static String toFastApiKorean(String standardKorean) {
		if (standardKorean == null) {
			return null;
		}

		// 역매핑: 표준 한글 → FastAPI 한글
		for (Map.Entry<String, String> entry : FASTAPI_TO_STANDARD.entrySet()) {
			if (entry.getValue().equals(standardKorean)) {
				return entry.getKey();
			}
		}

		// 매핑이 없으면 원본 그대로 반환
		return standardKorean;
	}

	/**
	 * 두 hazardType이 같은지 비교 (매핑 고려)
	 *
	 * 예: "극심한 고온"과 "폭염"은 같은 것으로 판단
	 *
	 * @param hazardType1 첫 번째 hazardType
	 * @param hazardType2 두 번째 hazardType
	 * @return 같으면 true
	 */
	public static boolean matches(String hazardType1, String hazardType2) {
		if (hazardType1 == null || hazardType2 == null) {
			return hazardType1 == hazardType2;
		}

		// 직접 비교
		if (hazardType1.equals(hazardType2)) {
			return true;
		}

		// 표준 형식으로 변환 후 비교
		String standard1 = toStandardKorean(hazardType1);
		String standard2 = toStandardKorean(hazardType2);

		return standard1.equals(standard2);
	}
}
