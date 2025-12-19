package com.skax.physicalrisk.dto.response.simulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * 위치 시뮬레이션 후보지 추천 응답 DTO
 *
 * Swagger v0.2 명세 준수
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "위치 시뮬레이션 후보지 추천 결과")
public class LocationRecommendationResponse {

	@Schema(description = "사업장 및 후보지 정보")
	private SiteWithCandidates site;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "사업장 및 후보지 정보")
	public static class SiteWithCandidates {
		@Schema(description = "사업장 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		private UUID siteId;

		@Schema(description = "첫 번째 추천 후보지")
		private CandidateLocation candidate1;

		@Schema(description = "두 번째 추천 후보지")
		private CandidateLocation candidate2;

		@Schema(description = "세 번째 추천 후보지")
		private CandidateLocation candidate3;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "후보지 위치 정보")
	public static class CandidateLocation {
		@Schema(description = "후보지 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		private UUID candidateId;

		@Schema(description = "후보지 이름", example = "세종 데이터 센터 부지")
		private String candidateName;

		@Schema(description = "위도", example = "36.5040736")
		private Double latitude;

		@Schema(description = "경도", example = "127.2494855")
		private Double longitude;

		@Schema(description = "지번 주소", example = "세종특별자치시 보람동 660")
		private String jibunAddress;

		@Schema(description = "도로명 주소", example = "세종특별자치시 한누리대로 2130 (보람동)")
		private String roadAddress;

		@Schema(description = "리스크 점수 (시나리오2-2040)", example = "70")
		private Integer riskscore;

		@Schema(description = "AAL 점수 (시나리오2-2040)", example = "20.5")
		private Float aalscore;

		@JsonProperty("physical-risk-scores")
		@Schema(description = "재해 유형별 물리적 리스크 점수",
			example = "{\"extreme_heat\": 10, \"extreme_cold\": 20, \"river_flood\": 30}")
		private Map<String, Integer> physicalRiskScores;

		@JsonProperty("aal-scores")
		@Schema(description = "재해 유형별 AAL 점수",
			example = "{\"extreme_heat\": 9.5, \"extreme_cold\": 10.2, \"river_flood\": 11.3}")
		private Map<String, Float> aalScores;

		@Schema(description = "장점", example = "홍수 위험 62% 감소한다")
		private String pros;

		@Schema(description = "단점", example = "초기 구축 비용 증가한다")
		private String cons;
	}
}
