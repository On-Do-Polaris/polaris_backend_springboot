package com.skax.physicalrisk.dto.request.site;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 보험 정보 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "보험 정보")
public class InsuranceInfo {

	@Schema(description = "보험 보전율", example = "0.8")
	private Double coverageRate;

	@Schema(description = "보험 상품", example = "화재보험")
	private String insuranceProduct;
}
