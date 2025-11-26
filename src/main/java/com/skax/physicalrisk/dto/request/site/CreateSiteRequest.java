package com.skax.physicalrisk.dto.request.site;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사업장 생성 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사업장 생성 요청")
public class CreateSiteRequest {

	@Schema(description = "사업장 이름", example = "서울 본사", required = true)
	@NotBlank(message = "사업장 이름은 필수입니다")
	private String name;

	@Schema(description = "위치 (시군구 단위)", example = "서울특별시 강남구", required = true)
	@NotBlank(message = "위치는 필수입니다")
	private String location;

	@Schema(description = "주소 (도로명 또는 지번)", example = "서울특별시 강남구 테헤란로 123", required = true)
	@NotBlank(message = "주소는 필수입니다")
	private String address;

	@Schema(description = "사업장 유형", example = "공장", required = true)
	@NotBlank(message = "사업장 유형은 필수입니다")
	private String type;
}
