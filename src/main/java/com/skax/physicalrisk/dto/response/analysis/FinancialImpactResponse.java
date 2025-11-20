package com.skax.physicalrisk.dto.response.analysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 시나리오별 재무 영향 분석 응답 DTO
 * SSP 시나리오별 기간(단기/중기/장기)에 따른 AAL 값 제공
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
@Schema(description = "시나리오별 재무 영향 분석")
public class FinancialImpactResponse {

	@Schema(description = "SSP 시나리오별 재무 영향 목록 (4개 시나리오)")
	private List<SSPScenarioImpact> scenarios;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "SSP 시나리오별 재무 영향")
	public static class SSPScenarioImpact {
		@Schema(description = "SSP 시나리오", example = "SSP1-2.6", allowableValues = {"SSP1-2.6", "SSP2-4.5", "SSP3-7.0", "SSP5-8.5"})
		private String scenario;

		@Schema(description = "리스크 종류", example = "폭염")
		private String riskType;

		@Schema(description = "단기 AAL (분기별: Q1, Q2, Q3, Q4)")
		private ShortTermAAL shortTerm;

		@Schema(description = "중기 AAL (연도별: 2026~2030)")
		private MidTermAAL midTerm;

		@Schema(description = "장기 AAL (연대별: 2020s, 2030s, 2040s, 2050s)")
		private LongTermAAL longTerm;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "단기 AAL (분기별)")
	public static class ShortTermAAL {
		@Schema(description = "1분기 AAL", example = "0.015")
		private Double q1;

		@Schema(description = "2분기 AAL", example = "0.018")
		private Double q2;

		@Schema(description = "3분기 AAL", example = "0.021")
		private Double q3;

		@Schema(description = "4분기 AAL", example = "0.019")
		private Double q4;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "중기 AAL (연도별)")
	public static class MidTermAAL {
		@Schema(description = "2026년 AAL", example = "0.023")
		private Double year2026;

		@Schema(description = "2027년 AAL", example = "0.025")
		private Double year2027;

		@Schema(description = "2028년 AAL", example = "0.027")
		private Double year2028;

		@Schema(description = "2029년 AAL", example = "0.029")
		private Double year2029;

		@Schema(description = "2030년 AAL", example = "0.031")
		private Double year2030;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "장기 AAL (연대별)")
	public static class LongTermAAL {
		@Schema(description = "2020년대 AAL", example = "0.028")
		private Double year2020s;

		@Schema(description = "2030년대 AAL", example = "0.035")
		private Double year2030s;

		@Schema(description = "2040년대 AAL", example = "0.042")
		private Double year2040s;

		@Schema(description = "2050년대 AAL", example = "0.051")
		private Double year2050s;
	}
}
