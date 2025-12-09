package com.skax.physicalrisk.dto.request.recommendation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 후보지 추천 배치 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "후보지 추천 배치 요청")
public class SiteRecommendationBatchRequest {

	@Schema(description = "작업 이름", example = "서울 지역 후보지 추천", required = true)
	@NotBlank(message = "작업 이름은 필수입니다")
	private String jobName;

	@Schema(description = "사업장 유형", example = "data_center", required = true)
	@NotBlank(message = "사업장 유형은 필수입니다")
	private String siteType;

	@Schema(description = "후보지 목록 (최소 1개)", required = true)
	@NotEmpty(message = "후보지 목록은 최소 1개 이상이어야 합니다")
	private List<CandidateSite> candidates;

	@Schema(description = "추천 기준")
	private RecommendationCriteria criteria;

	@Schema(description = "기준 사업장 ID (비교 기준으로 사용)")
	private UUID referenceSiteId;
}
