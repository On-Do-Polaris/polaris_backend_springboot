package com.skax.physicalrisk.client.fastapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * FastAPI 분석 시작 요청 DTO
 *
 * FastAPI POST /api/analysis/start 엔드포인트용
 * 단일/다중 사업장 분석 모두 지원
 *
 * 최종 수정일: 2025-12-18
 * 파일 버전: v03
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartAnalysisRequestDto {

	private UUID userId;  // 사용자 ID (필수)
	private List<SiteInfoDto> sites;  // 사업장 리스트 (단일/다중 모두 지원)
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
