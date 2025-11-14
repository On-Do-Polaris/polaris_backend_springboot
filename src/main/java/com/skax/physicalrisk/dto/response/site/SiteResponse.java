package com.skax.physicalrisk.dto.response.site;

import com.skax.physicalrisk.domain.site.entity.Site;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사업장 응답 DTO
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteResponse {

	private UUID id;
	private String name;
	private String address;
	private String city;
	private Double latitude;
	private Double longitude;
	private String industry;
	private String mainHazard;
	private Integer riskScore;
	private Site.RiskLevel riskLevel;
	private LocalDateTime lastAnalyzedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	/**
	 * 엔티티로부터 응답 DTO 생성
	 *
	 * @param site 사업장 엔티티
	 * @return SiteResponse
	 */
	public static SiteResponse from(Site site) {
		return SiteResponse.builder()
			.id(site.getId())
			.name(site.getName())
			.address(site.getAddress())
			.city(site.getCity())
			.latitude(site.getLatitude())
			.longitude(site.getLongitude())
			.industry(site.getIndustry())
			.mainHazard(site.getMainHazard())
			.riskScore(site.getRiskScore())
			.riskLevel(site.getRiskLevel())
			.lastAnalyzedAt(site.getLastAnalyzedAt())
			.createdAt(site.getCreatedAt())
			.updatedAt(site.getUpdatedAt())
			.build();
	}
}
