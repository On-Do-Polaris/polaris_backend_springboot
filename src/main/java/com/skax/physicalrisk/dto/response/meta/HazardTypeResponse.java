package com.skax.physicalrisk.dto.response.meta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 위험 유형 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "위험 유형 정보")
public class HazardTypeResponse {

	@Schema(description = "위험 유형 코드", example = "extreme_heat")
	private String code;

	@Schema(description = "위험 유형명 (한글)", example = "극심한 고온")
	private String name;

	@Schema(description = "위험 유형명 (영문)", example = "Extreme Heat")
	private String nameEn;

	@Schema(description = "카테고리", example = "기온", allowableValues = {"기온", "강수", "바람", "복합"})
	private String category;

	@Schema(description = "설명", example = "극심한 고온 현상으로 인한 위험")
	private String description;
}
