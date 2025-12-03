package com.skax.physicalrisk.client.fastapi.dto;

import com.skax.physicalrisk.domain.site.entity.Site;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * FastAPI SiteInfo DTO
 *
 * FastAPI 서버의 SiteInfo 스키마에 매핑되는 DTO
 *
 * 최종 수정일: 2025-11-18
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteInfoDto {

	private UUID id;
	private String name;
	private String address;
	private Double latitude;
	private Double longitude;
	private String industry;

	/**
	 * Site 엔티티로부터 DTO 생성
	 *
	 * @param site 사업장 엔티티
	 * @return SiteInfoDto
	 */
	public static SiteInfoDto from(Site site) {
		String address = site.getRoadAddress() != null ? site.getRoadAddress() : site.getJibunAddress();

		return SiteInfoDto.builder()
			.id(site.getId())
			.name(site.getName())
			.address(address)
			.latitude(site.getLatitude())
			.longitude(site.getLongitude())
			.industry(site.getType())
			.build();
	}
}
