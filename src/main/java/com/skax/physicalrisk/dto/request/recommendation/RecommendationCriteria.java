package com.skax.physicalrisk.dto.request.recommendation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 추천 기준 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "추천 기준")
public class RecommendationCriteria {

	@Schema(description = "위험 요인 가중치 (hazard_code -> 가중치)", example = "{\"extreme_heat\": 0.3, \"river_flood\": 0.5}")
	private Map<String, Double> hazardWeights;

	@Schema(description = "제외할 위험 요인 코드 목록", example = "[\"wildfire\", \"coastal_flood\"]")
	private List<String> excludedHazards;

	@Schema(description = "최소 위험 점수 (이하는 제외)", example = "30.0")
	private Double minRiskScore;

	@Schema(description = "최대 위험 점수 (이상은 제외)", example = "80.0")
	private Double maxRiskScore;

	@Schema(description = "추가 필터 조건 (JSONB)", example = "{\"region\": \"서울\"}")
	private Map<String, Object> additionalFilters;
}
