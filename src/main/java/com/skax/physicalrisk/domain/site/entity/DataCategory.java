package com.skax.physicalrisk.domain.site.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 추가 데이터 카테고리 열거형
 *
 * 최종 수정일: 2025-12-09
 * 파일 버전: v01 - ERD 기준 데이터 카테고리 정의
 *
 * site_additional_data 테이블의 data_category 필드에 사용
 * ERD 문서 기준: building/asset/power/insurance/custom
 *
 * @author SKAX Team
 */
@Getter
@RequiredArgsConstructor
public enum DataCategory {

	BUILDING("building", "건물 정보"),
	ASSET("asset", "자산 정보"),
	POWER("power", "전력 사용량"),
	INSURANCE("insurance", "보험 정보"),
	CUSTOM("custom", "사용자 정의");

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
	 * @param code 데이터 카테고리 코드
	 * @return DataCategory enum
	 */
	@JsonCreator
	public static DataCategory fromCode(String code) {
		for (DataCategory category : DataCategory.values()) {
			if (category.code.equalsIgnoreCase(code)) {
				return category;
			}
		}
		throw new IllegalArgumentException("Unknown data category code: " + code);
	}
}
