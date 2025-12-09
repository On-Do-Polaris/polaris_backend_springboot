package com.skax.physicalrisk.dto.request.site;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 자산 정보 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자산 정보")
public class AssetInfo {

	@Schema(description = "총 자산 가치 (원)", example = "50000000000")
	private Long totalAssetValue;

	@Schema(description = "직원 수", example = "200")
	private Integer employeeCount;
}
