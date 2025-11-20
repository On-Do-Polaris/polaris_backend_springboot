package com.skax.physicalrisk.dto.response.meta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSP 시나리오 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SSP 시나리오 정보")
public class SSPScenarioResponse {

	@Schema(description = "시나리오 코드", example = "SSP2-4.5")
	private String code;

	@Schema(description = "시나리오명", example = "중간 경로")
	private String name;

	@Schema(description = "설명", example = "중간 수준의 사회경제적 도전과 중간 수준의 복사강제력")
	private String description;
}
