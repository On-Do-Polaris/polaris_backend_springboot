package com.skax.physicalrisk.dto.request.simulation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 사업장 이전 시뮬레이션 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사업장 이전 시뮬레이션 요청")
public class RelocationSimulationRequest {

	@Schema(description = "현재 사업장 ID", required = true)
	@NotNull(message = "현재 사업장 ID는 필수입니다")
	private UUID currentSiteId;

	@Schema(description = "이전될 위치의 위도", example = "37.5665", required = true)
	@NotNull(message = "위도는 필수입니다")
	private BigDecimal latitude;

	@Schema(description = "이전될 위치의 경도", example = "126.9780", required = true)
	@NotNull(message = "경도는 필수입니다")
	private BigDecimal longitude;

	@Schema(description = "이전될 위치의 도로명 주소", example = "서울특별시 강남구 테헤란로 123")
	private String roadAddress;

	@Schema(description = "이전될 위치의 지번 주소", example = "서울특별시 강남구 역삼동 123-45")
	private String jibunAddress;
}
