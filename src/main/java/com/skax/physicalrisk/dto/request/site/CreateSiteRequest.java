package com.skax.physicalrisk.dto.request.site;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사업장 생성 요청 DTO
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSiteRequest {

	@NotBlank(message = "사업장 이름은 필수입니다")
	private String name;

	@NotBlank(message = "주소는 필수입니다")
	private String address;

	@NotBlank(message = "산업 분류는 필수입니다")
	private String industry;

	private Double latitude;
	private Double longitude;
	private String description;
}
