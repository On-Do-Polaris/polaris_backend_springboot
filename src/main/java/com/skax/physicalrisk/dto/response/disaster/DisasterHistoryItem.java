package com.skax.physicalrisk.dto.response.disaster;

import com.skax.physicalrisk.domain.disaster.entity.DisasterSeverity;
import com.skax.physicalrisk.domain.disaster.entity.DisasterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 재해 이력 항목 DTO
 *
 * 목록 조회용 간소화된 형태
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "재해 이력 항목")
public class DisasterHistoryItem {

	@Schema(description = "재해연보 ID", example = "12345")
	private Integer yearbookId;

	@Schema(description = "연도", example = "2023")
	private Integer year;

	@Schema(description = "행정구역 코드", example = "11110")
	private String adminCode;

	@Schema(description = "재해 유형", example = "TYPHOON")
	private DisasterType disasterType;

	@Schema(description = "총 피해액 (억원)", example = "460.5")
	private Double totalDamage;

	@Schema(description = "피해 수준", example = "SEVERE")
	private DisasterSeverity damageLevel;

	@Schema(description = "피해 건물 수", example = "150")
	private Integer affectedBuildings;

	@Schema(description = "피해 인구", example = "5000")
	private Integer affectedPopulation;
}
