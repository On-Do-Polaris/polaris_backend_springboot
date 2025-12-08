package com.skax.physicalrisk.dto.request.analysis;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 분석 시작 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "분석 시작 요청")
public class StartAnalysisRequest {

	@Schema(description = "위도", example = "37.5665", required = true)
	@NotNull(message = "위도는 필수입니다")
	private BigDecimal latitude;

	@Schema(description = "경도", example = "126.9780", required = true)
	@NotNull(message = "경도는 필수입니다")
	private BigDecimal longitude;

	@Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123")
	private String roadAddress;

	@Schema(description = "지번 주소", example = "서울특별시 강남구 역삼동 123-45")
	private String jibunAddress;

	@Schema(description = "사업장 타입", example = "공장", required = true)
	@NotBlank(message = "사업장 타입은 필수입니다")
	private String siteType;
}
