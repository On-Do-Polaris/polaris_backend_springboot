package com.skax.physicalrisk.dto.request.simulation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 기후 시뮬레이션 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기후 시뮬레이션 요청")
public class ClimateSimulationRequest {

	@Schema(description = "SSP 시나리오", example = "SSP2-4.5", allowableValues = {"SSP1-2.6", "SSP2-4.5", "SSP5-8.5"})
	@NotBlank(message = "SSP 시나리오는 필수입니다")
	private String scenario;

	@Schema(description = "위험 유형", example = "극심한 고온")
	@NotBlank(message = "위험 유형은 필수입니다")
	private String hazardType;

	@Schema(description = "시뮬레이션 대상 사업장 ID 목록")
	@NotEmpty(message = "사업장 ID는 최소 1개 이상 필요합니다")
	private List<UUID> siteIds;

	@Schema(description = "시작 연도", example = "2024")
	@NotNull(message = "시작 연도는 필수입니다")
	private Integer startYear;
}
