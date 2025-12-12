package com.skax.physicalrisk.dto.response.simulation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 기후 시뮬레이션 응답 DTO
 * 연도별로 전국 평균 기온 및 사업장별 데이터 제공
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기후 시뮬레이션 결과")
public class ClimateSimulationResponse {

	@Schema(description = "SSP 시나리오", example = "SSP2-4.5")
	private String scenario;

	@Schema(description = "리스크 유형", example = "극심한 고온")
	private String riskType;

	@Schema(description = "연도별 시뮬레이션 데이터")
	private List<YearlyData> yearlyData;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "연도별 데이터")
	public static class YearlyData {
		@Schema(description = "연도", example = "2030")
		private Integer year;

		@Schema(description = "전국 평균 기온 (°C)", example = "14.5")
		private Double nationalAverageTemperature;

		@Schema(description = "사업장별 데이터")
		private List<SiteData> sites;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "사업장 데이터")
	public static class SiteData {
		@Schema(description = "사업장 ID")
		private UUID siteId;

		@Schema(description = "사업장 이름", example = "서울 본사")
		private String siteName;

		@Schema(description = "사업장 위치 평균 기온 (°C)", example = "15")
		private Integer Temperature;

		@Schema(description = "리스크 증가율", example = "15.2")
		private Double riskincrease;
	}
}
