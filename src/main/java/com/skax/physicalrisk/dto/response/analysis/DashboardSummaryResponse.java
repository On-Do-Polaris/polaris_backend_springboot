package com.skax.physicalrisk.dto.response.analysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 대시보드 요약 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대시보드 요약 정보")
public class DashboardSummaryResponse {

	@Schema(description = "전체 사업장의 주요 기후 리스크", example = "극심한 고온")
	private String mainClimateRisk;

	@Schema(description = "사업장 목록")
	private List<SiteSummary> sites;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "사업장 요약")
	public static class SiteSummary {
		@Schema(description = "사업장 ID")
		private UUID siteId;

		@Schema(description = "사업장 이름", example = "서울 본사")
		private String siteName;

		@Schema(description = "사업장 유형", example = "공장")
		private String siteType;

		@Schema(description = "위치", example = "서울특별시 강남구")
		private String location;

		@Schema(description = "통합 리스크 점수 (0-100)", example = "75")
		private Integer totalRiskScore;
	}
}
