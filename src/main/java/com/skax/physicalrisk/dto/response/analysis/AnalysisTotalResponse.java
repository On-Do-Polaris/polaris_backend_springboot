package com.skax.physicalrisk.dto.response.analysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 사업장 상세 분석 결과 응답 DTO
 * 9대 물리적 리스크에 대한 종합 분석
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사업장 상세 분석 결과")
public class AnalysisTotalResponse {

	@Schema(description = "사업장 ID")
	private UUID siteId;

	@Schema(description = "사업장 이름", example = "서울 본사")
	private String siteName;

	@Schema(description = "9대 물리적 리스크 분석 결과")
	private List<PhysicalRiskDetail> physicalRisks;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "물리적 리스크 상세")
	public static class PhysicalRiskDetail {
		@Schema(description = "리스크 유형", example = "극심한 고온")
		private String riskType;

		@Schema(description = "리스크 점수", minimum = "0", maximum = "100", example = "75")
		private Integer riskScore;

		@Schema(description = "재무 손실률 (0.0-1.0)", example = "0.023")
		private Double financialLossRate;
	}
}
