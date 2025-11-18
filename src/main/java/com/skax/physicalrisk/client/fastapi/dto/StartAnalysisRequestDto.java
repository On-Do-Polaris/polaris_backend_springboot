package com.skax.physicalrisk.client.fastapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * FastAPI 분석 시작 요청 DTO
 *
 * FastAPI POST /api/v1/analysis/start 엔드포인트용
 *
 * 최종 수정일: 2025-11-18
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartAnalysisRequestDto {

	private SiteInfoDto site;
	private List<String> hazardTypes;
	private String priority;  // low, normal, high
	private AnalysisOptions options;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class AnalysisOptions {
		private Boolean includeFinancialImpact;
		private Boolean includeVulnerability;
		private Boolean includePastEvents;
		private List<String> sspScenarios;  // [SSP2-4.5, SSP5-8.5]
	}
}
