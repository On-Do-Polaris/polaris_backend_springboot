package com.skax.physicalrisk.dto.response.recommendation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 추천 후보지 항목 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "추천 후보지 항목")
public class RecommendationItem {

	@Schema(description = "후보지 이름", example = "강남 후보지")
	private String name;

	@Schema(description = "위도", example = "37.5665")
	private BigDecimal latitude;

	@Schema(description = "경도", example = "126.978")
	private BigDecimal longitude;

	@Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123")
	private String roadAddress;

	@Schema(description = "추천 순위", example = "1")
	private Integer rank;

	@Schema(description = "종합 위험 점수 (0-100)", example = "45.5")
	private Double overallRiskScore;

	@Schema(description = "위험 요인별 점수 (hazard_code -> 점수)", example = "{\"extreme_heat\": 50.0, \"river_flood\": 40.0}")
	private Map<String, Double> hazardScores;

	@Schema(description = "추천 이유", example = "홍수 위험이 낮고 폭염 위험 중간 수준")
	private String recommendation;

	@Schema(description = "추가 분석 결과 (JSONB)", example = "{\"distance_to_reference\": 10.5}")
	private Map<String, Object> analysisDetails;
}
