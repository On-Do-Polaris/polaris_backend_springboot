package com.skax.physicalrisk.domain.disaster.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 재해 유형 열거형
 *
 * 최종 수정일: 2025-12-09
 * 파일 버전: v01 - ERD 기준 재해 유형 정의
 *
 * api_disaster_yearbook 테이블의 disaster_type 필드에 사용
 * ERD 문서 기준: 태풍/호우/대설/강풍/풍랑/지진/기타
 *
 * @author SKAX Team
 */
@Getter
@RequiredArgsConstructor
public enum DisasterType {

	TYPHOON("TYPHOON", "태풍"),
	HEAVY_RAIN("HEAVY_RAIN", "호우"),
	HEAVY_SNOW("HEAVY_SNOW", "대설"),
	STRONG_WIND("STRONG_WIND", "강풍"),
	WIND_WAVE("WIND_WAVE", "풍랑"),
	EARTHQUAKE("EARTHQUAKE", "지진"),
	OTHER("OTHER", "기타");

	private final String code;
	private final String description;

	/**
	 * JSON 직렬화 시 code 값을 사용
	 */
	@JsonValue
	public String getCode() {
		return code;
	}

	/**
	 * JSON 역직렬화 시 code 값으로 enum을 찾음
	 *
	 * @param code 재해 유형 코드
	 * @return DisasterType enum
	 */
	@JsonCreator
	public static DisasterType fromCode(String code) {
		for (DisasterType type : DisasterType.values()) {
			if (type.code.equalsIgnoreCase(code)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown disaster type code: " + code);
	}
}
