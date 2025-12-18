package com.skax.physicalrisk.dto.response.past;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 과거 재해 이력 응답 DTO
 *
 * 최종 수정일: 2025-12-18
 * 파일 버전: v03 - FastAPI 응답 필드명에 맞게 수정 (alertDate, disasterType)
 *
 * @author SKAX Team
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PastDisasterResponse {

	/**
	 * 재해 이력 목록 (FastAPI는 data를 배열로 직접 반환)
	 */
	private List<DisasterItem> data;

	/**
	 * 재해 이력 항목
	 */
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class DisasterItem {
		/**
		 * 재해 ID
		 */
		private Integer id;

		/**
		 * 발생 날짜 (FastAPI에서 alertDate로 반환)
		 */
		private String alertDate;

		/**
		 * 재해 유형 (FastAPI에서 disasterType으로 반환)
		 */
		private String disasterType;

		/**
		 * 심각도
		 */
		private String severity;

		/**
		 * 영향을 받은 지역 (FastAPI는 단일 문자열로 반환)
		 */
		private String region;
	}
}
