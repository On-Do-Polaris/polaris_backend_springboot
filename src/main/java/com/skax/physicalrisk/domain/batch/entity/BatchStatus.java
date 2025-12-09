package com.skax.physicalrisk.domain.batch.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 배치 작업 상태 열거형
 *
 * 최종 수정일: 2025-12-09
 * 파일 버전: v01 - ERD 기준 배치 작업 상태 정의
 *
 * batch_jobs 테이블의 status 필드에 사용
 * ERD 문서 기준: queued/running/completed/failed/cancelled
 *
 * @author SKAX Team
 */
@Getter
@RequiredArgsConstructor
public enum BatchStatus {

	QUEUED("queued", "대기 중"),
	RUNNING("running", "실행 중"),
	COMPLETED("completed", "완료"),
	FAILED("failed", "실패"),
	CANCELLED("cancelled", "취소됨");

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
	 * @param code 배치 작업 상태 코드
	 * @return BatchStatus enum
	 */
	@JsonCreator
	public static BatchStatus fromCode(String code) {
		for (BatchStatus status : BatchStatus.values()) {
			if (status.code.equalsIgnoreCase(code)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown batch status code: " + code);
	}
}
