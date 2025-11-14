package com.skax.physicalrisk.dto.request.site;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사업장 수정 요청 DTO
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
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
	private String description;
}
