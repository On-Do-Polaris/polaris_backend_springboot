package com.skax.physicalrisk.dto.request.site;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사업장 수정 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사업장 수정 요청")
public class UpdateSiteRequest {

	@Schema(description = "사업장 이름", example = "서울 본사")
	private String name;

	@Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123")
	private String roadAddress;

	@Schema(description = "지번 주소", example = "서울특별시 강남구 역삼동 123-45")
	private String jibunAddress;

	@Schema(description = "위도", example = "37.5665")
	private Double latitude;

	@Schema(description = "경도", example = "126.978")
	private Double longitude;

	@Schema(description = "사업장 유형", example = "data_center")
	private String type;
}
