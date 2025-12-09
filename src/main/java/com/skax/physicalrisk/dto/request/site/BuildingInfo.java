package com.skax.physicalrisk.dto.request.site;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 건물 정보 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "건물 정보")
public class BuildingInfo {

	@Schema(description = "건물 연식 (년)", example = "25")
	private Integer buildingAge;

	@Schema(description = "건물 구조", example = "철근콘크리트")
	private String buildingType;

	@Schema(description = "내진 설계 여부", example = "true")
	private Boolean seismicDesign;

	@Schema(description = "연면적 (m²)", example = "5000.0")
	private Double grossFloorArea;
}
