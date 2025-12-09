package com.skax.physicalrisk.dto.request.simulation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 기후 시뮬레이션 요청 DTO
 *
 * 현재 사용자의 모든 사업장에 대해 2020-2100년 기간의 시뮬레이션을 자동 실행
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기후 시뮬레이션 요청")
public class ClimateSimulationRequest {

	@Schema(description = "SSP 시나리오", example = "SSP2-4.5", allowableValues = {"SSP1-2.6", "SSP2-4.5", "SSP3-7.0", "SSP5-8.5"})
	@NotBlank(message = "SSP 시나리오는 필수입니다")
	@Pattern(regexp = "SSP1-2\\.6|SSP2-4\\.5|SSP3-7\\.0|SSP5-8\\.5", message = "유효한 SSP 시나리오를 입력하세요")
	private String scenario;

	@Schema(description = "위험 유형", example = "극심한 고온")
	@NotBlank(message = "위험 유형은 필수입니다")
	private String hazardType;
}
