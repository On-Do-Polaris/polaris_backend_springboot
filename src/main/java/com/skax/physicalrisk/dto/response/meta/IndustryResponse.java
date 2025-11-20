package com.skax.physicalrisk.dto.response.meta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 산업 분류 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "산업 분류 정보")
public class IndustryResponse {

	@Schema(description = "산업 코드", example = "MANUFACTURING")
	private String code;

	@Schema(description = "산업명", example = "제조업")
	private String name;

	@Schema(description = "설명", example = "제품 생산 및 가공 관련 산업")
	private String description;
}
