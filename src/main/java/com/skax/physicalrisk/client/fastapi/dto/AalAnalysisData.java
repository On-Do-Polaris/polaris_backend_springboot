package com.skax.physicalrisk.client.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AAL v11 분석 데이터 DTO
 * FastAPI로부터 받는 aal_analysis 응답 매핑
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AalAnalysisData {

	/**
	 * 기본 AAL (Service에서 계산)
	 */
	@JsonProperty("base_aal")
	private Double baseAal;

	/**
	 * 취약성 스케일 계수 (F_vuln)
	 */
	@JsonProperty("vulnerability_scale")
	private Double vulnerabilityScale;

	/**
	 * 최종 AAL 백분율 (% 단위)
	 * 예: 2.34 (2.34%를 의미)
	 */
	@JsonProperty("final_aal_percentage")
	private Double finalAalPercentage;

	/**
	 * 보험 보전율
	 */
	@JsonProperty("insurance_rate")
	private Double insuranceRate;

	/**
	 * 위험 수준
	 */
	@JsonProperty("risk_level")
	private String riskLevel;

	/**
	 * final_aal_percentage를 0-1 스케일로 변환
	 * 예: 2.34% → 0.0234
	 *
	 * @return 0-1 스케일 AAL
	 */
	public Double getAalRate() {
		if (finalAalPercentage == null) {
			return 0.0;
		}
		return finalAalPercentage / 100.0;
	}
}
