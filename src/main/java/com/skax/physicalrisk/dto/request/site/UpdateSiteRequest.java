package com.skax.physicalrisk.dto.request.site;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사업장 수정 요청 DTO
 *
 * 최종 수정일: 2025-11-18
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSiteRequest {

	private String name;
	private String address;
	private String industry;
	private Double latitude;
	private Double longitude;

	// AI 분석용 추가 필드
	private Integer buildingAge;
	private String buildingType;
	private Boolean seismicDesign;
	private Double floorArea;
	private Double assetValue;
	private Integer employeeCount;

	private String description;
}
