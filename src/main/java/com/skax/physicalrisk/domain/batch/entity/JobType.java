package com.skax.physicalrisk.domain.batch.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 배치 작업 유형 열거형
 *
 * 최종 수정일: 2025-12-09
 * 파일 버전: v01 - ERD 기준 배치 작업 유형 정의
 *
 * batch_jobs 테이블의 job_type 필드에 사용
 * ERD 문서 기준: site_recommendation/bulk_analysis/data_export
 *
 * @author SKAX Team
 */
@Getter
@RequiredArgsConstructor
public enum JobType {

	SITE_RECOMMENDATION("site_recommendation", "후보지 추천"),
	BULK_ANALYSIS("bulk_analysis", "대량 분석"),
	DATA_EXPORT("data_export", "데이터 내보내기");

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
	 * @param code 배치 작업 유형 코드
	 * @return JobType enum
	 */
	@JsonCreator
	public static JobType fromCode(String code) {
		for (JobType type : JobType.values()) {
			if (type.code.equalsIgnoreCase(code)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown job type code: " + code);
	}
}
