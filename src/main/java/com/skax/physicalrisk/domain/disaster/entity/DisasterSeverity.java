package com.skax.physicalrisk.domain.disaster.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 재해 심각도 열거형
 *
 * 최종 수정일: 2025-12-09
 * 파일 버전: v01 - ERD 기준 재해 심각도 정의
 *
 * api_disaster_yearbook 테이블의 damage_level 필드에 사용
 * ERD 문서 기준: 경미/보통/심각/대재해
 *
 * @author SKAX Team
 */
@Getter
@RequiredArgsConstructor
public enum DisasterSeverity {

	MINOR("MINOR", "경미"),
	MODERATE("MODERATE", "보통"),
	SEVERE("SEVERE", "심각"),
	CATASTROPHIC("CATASTROPHIC", "대재해");

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
	 * @param code 재해 심각도 코드
	 * @return DisasterSeverity enum
	 */
	@JsonCreator
	public static DisasterSeverity fromCode(String code) {
		for (DisasterSeverity severity : DisasterSeverity.values()) {
			if (severity.code.equalsIgnoreCase(code)) {
				return severity;
			}
		}
		throw new IllegalArgumentException("Unknown disaster severity code: " + code);
	}
}
