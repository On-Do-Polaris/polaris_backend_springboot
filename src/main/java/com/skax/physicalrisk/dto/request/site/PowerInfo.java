package com.skax.physicalrisk.dto.request.site;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 전력 정보 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전력 정보")
public class PowerInfo {

	@Schema(description = "IT 전력 사용량 (kW)", example = "1000.0")
	private Double itPower;

	@Schema(description = "냉각 전력 사용량 (kW)", example = "500.0")
	private Double coolingPower;

	@Schema(description = "총 전력 사용량 (kW)", example = "1500.0")
	private Double totalPower;
}
