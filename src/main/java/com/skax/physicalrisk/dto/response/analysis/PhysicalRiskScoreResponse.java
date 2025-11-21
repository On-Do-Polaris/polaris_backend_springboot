package com.skax.physicalrisk.dto.response.analysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 시나리오별 물리적 리스크 점수 응답 DTO
 * SSP 시나리오별 기간(단기/중기/장기)에 따른 리스크 점수 제공
 * - 단기: 1~4분기
 * - 중기: 2026~2030년
 * - 장기: 2020s, 2030s, 2040s, 2050s
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시나리오별 물리적 리스크 점수")
public class PhysicalRiskScoreResponse {

	@Schema(description = "SSP 시나리오별 리스크 점수 목록 (4개 시나리오)")
	private List<SSPScenarioScore> scenarios;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "SSP 시나리오별 리스크 점수")
	public static class SSPScenarioScore {
		@Schema(description = "SSP 시나리오", example = "SSP1-2.6", allowableValues = {"SSP1-2.6", "SSP2-4.5", "SSP3-7.0", "SSP5-8.5"})
		private String scenario;

		@Schema(description = "리스크 종류", example = "극심한 고온")
		private String riskType;

		@Schema(description = "단기 리스크 점수 (분기별: Q1, Q2, Q3, Q4)")
		private ShortTermScore shortTerm;

		@Schema(description = "중기 리스크 점수 (연도별: 2026~2030)")
		private MidTermScore midTerm;

		@Schema(description = "장기 리스크 점수 (연대별: 2020s, 2030s, 2040s, 2050s)")
		private LongTermScore longTerm;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "단기 리스크 점수 (분기별)")
	public static class ShortTermScore {
		@Schema(description = "1분기 점수 (0-100)", example = "65")
		private Integer q1;

		@Schema(description = "2분기 점수 (0-100)", example = "72")
		private Integer q2;

		@Schema(description = "3분기 점수 (0-100)", example = "78")
		private Integer q3;

		@Schema(description = "4분기 점수 (0-100)", example = "70")
		private Integer q4;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "중기 리스크 점수 (연도별)")
	public static class MidTermScore {
		@Schema(description = "2026년 점수 (0-100)", example = "68")
		private Integer year2026;

		@Schema(description = "2027년 점수 (0-100)", example = "70")
		private Integer year2027;

		@Schema(description = "2028년 점수 (0-100)", example = "73")
		private Integer year2028;

		@Schema(description = "2029년 점수 (0-100)", example = "75")
		private Integer year2029;

		@Schema(description = "2030년 점수 (0-100)", example = "77")
		private Integer year2030;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "장기 리스크 점수 (연대별)")
	public static class LongTermScore {
		@Schema(description = "2020년대 점수 (0-100)", example = "72")
		private Integer year2020s;

		@Schema(description = "2030년대 점수 (0-100)", example = "78")
		private Integer year2030s;

		@Schema(description = "2040년대 점수 (0-100)", example = "84")
		private Integer year2040s;

		@Schema(description = "2050년대 점수 (0-100)", example = "89")
		private Integer year2050s;
	}
}
