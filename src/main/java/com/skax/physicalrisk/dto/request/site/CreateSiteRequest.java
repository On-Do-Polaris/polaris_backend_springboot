package com.skax.physicalrisk.dto.request.site;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사업장 생성 요청 DTO
 *
 * 최종 수정일: 2025-11-18
 * 파일 버전: v02
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

	@NotNull(message = "위도는 필수입니다")
	private Double latitude;

	@NotNull(message = "경도는 필수입니다")
	private Double longitude;

	// AI 분석용 추가 필드
	private Integer buildingAge;        // 건물 연령 (년)
	private String buildingType;        // 건물 유형
	private Boolean seismicDesign;      // 내진설계 여부
	private Double floorArea;           // 연면적 (m²)
	private Double assetValue;          // 자산 가치 (KRW)
	private Integer employeeCount;      // 직원 수

	private String description;
}
