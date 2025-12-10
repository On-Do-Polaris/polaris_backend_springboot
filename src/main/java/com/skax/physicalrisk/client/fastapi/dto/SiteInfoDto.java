package com.skax.physicalrisk.client.fastapi.dto;

import com.skax.physicalrisk.domain.site.entity.Site;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * FastAPI SiteInfo DTO
 *
 * FastAPI 서버의 SiteInfo 스키마에 매핑되는 DTO
 *
 * 최종 수정일: 2025-12-10
 * 파일 버전: v02 - Industry 매핑 로직 추가
 *
 * @author SKAX Team
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteInfoDto {

	private UUID id;
	private String name;
	private String address;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private String industry;

	/**
	 * Site 엔티티로부터 DTO 생성
	 *
	 * @param site 사업장 엔티티
	 * @return SiteInfoDto
	 */
	public static SiteInfoDto from(Site site) {
		String address = site.getRoadAddress() != null ? site.getRoadAddress() : site.getJibunAddress();

		// Industry 값 변환 (DB 값 → FastAPI 형식)
		String industry = mapSiteTypeToIndustry(site.getType());

		return SiteInfoDto.builder()
			.id(site.getId())
			.name(site.getName())
			.address(address)
			.latitude(site.getLatitude())
			.longitude(site.getLongitude())
			.industry(industry)
			.build();
	}

	/**
	 * Site의 type을 FastAPI industry 형식으로 변환
	 *
	 * FastAPI가 기대하는 값: data_center, factory, office, warehouse, retail
	 *
	 * @param siteType Site 엔티티의 type 필드
	 * @return FastAPI가 인식하는 industry 값
	 */
	private static String mapSiteTypeToIndustry(String siteType) {
		if (siteType == null) {
			log.warn("Site type is null. Using default 'office'");
			return "office";  // 기본값
		}

		// 대소문자 구분 없이 매핑
		String normalized = siteType.toLowerCase().trim();

		switch (normalized) {
			case "데이터센터":
			case "data_center":
			case "datacenter":
				return "data_center";

			case "제조업":
			case "공장":
			case "factory":
			case "manufacturing":
				return "factory";

			case "사무실":
			case "본사":
			case "office":
				return "office";

			case "창고":
			case "warehouse":
			case "물류센터":
			case "물류":
			case "logistics":
				return "warehouse";

			case "매장":
			case "retail":
			case "소매":
			case "유통":
				return "retail";

			default:
				log.warn("Unknown site type: '{}'. Using default 'office'", siteType);
				return "office";  // 알 수 없는 값은 기본값
		}
	}
}
