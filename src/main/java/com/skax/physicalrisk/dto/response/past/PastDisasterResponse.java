package com.skax.physicalrisk.dto.response.past;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 과거 재해 이력 응답 DTO
 *
 * 최종 수정일: 2025-12-10
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PastDisasterResponse {

	/**
	 * 데이터 래퍼
	 */
	private DataWrapper data;

	/**
	 * 데이터 래퍼 클래스
	 */
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class DataWrapper {
		/**
		 * 재해 이력 목록
		 */
		private List<DisasterItem> items;
	}

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
		 * 발생 날짜
		 */
		private String date;

		/**
		 * 재해 유형
		 */
		private String disaster_type;

		/**
		 * 심각도
		 */
		private String severity;

		/**
		 * 영향을 받은 지역 목록
		 */
		private List<String> region;
	}
}
