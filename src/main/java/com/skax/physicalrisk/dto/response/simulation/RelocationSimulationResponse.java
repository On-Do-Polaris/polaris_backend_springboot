package com.skax.physicalrisk.dto.response.simulation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 사업장 이전 시뮬레이션 응답 DTO
 * 기존 위치와 이전될 위치의 리스크별 점수 및 AAL 비교
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사업장 이전 시뮬레이션 결과")
public class RelocationSimulationResponse {

	@Schema(description = "기존 위치 정보")
	private LocationData currentLocation;

	@Schema(description = "이전될 위치 정보")
	private LocationData newLocation;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "위치별 리스크 데이터")
	public static class LocationData {
		@Schema(description = "리스크별 분석 결과")
		private List<RiskData> risks;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "리스크별 데이터")
	public static class RiskData {
		@Schema(description = "리스크 유형", example = "극심한 고온")
		private String riskType;

		@Schema(description = "리스크 점수 (0-100)", example = "75")
		private Integer riskScore;

		@Schema(description = "AAL (연평균 자산 손실률, 0.0-1.0)", example = "0.023")
		private Double aal;
	}
}
