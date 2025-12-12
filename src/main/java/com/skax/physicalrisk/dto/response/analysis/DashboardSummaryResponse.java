package com.skax.physicalrisk.dto.response.analysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

		@Schema(description = "사업장 이름", example = "sk u 타워")
		private String siteName;

		@Schema(description = "위도", example = "37.36633726")
		private BigDecimal latitude;

		@Schema(description = "경도", example = "127.10661717")
		private BigDecimal longitude;

		@Schema(description = "지번 주소", example = "경기도 성남시 분당구 정자동 25-1")
		private String jibunAddress;

		@Schema(description = "도로명 주소", example = "경기도 성남시 분당구 성남대로343번길 9")
		private String roadAddress;

		@Schema(description = "사업장 유형", example = "data_center")
		private String siteType;

		@Schema(description = "통합 리스크 점수 (0-100)", example = "75")
		private Integer totalRiskScore;
	}
}
