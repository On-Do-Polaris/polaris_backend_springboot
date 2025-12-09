package com.skax.physicalrisk.dto.response.site;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 사업장 목록 응답 DTO
 * 사용자가 등록한 전체 사업장의 기본 정보
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사업장 목록")
public class SiteResponse {

	@Schema(description = "사업장 목록")
	private List<SiteInfo> sites;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@Schema(description = "사업장 정보")
	public static class SiteInfo {
		@Schema(description = "사업장 ID")
		private UUID siteId;

		@Schema(description = "사업장 이름", example = "서울 본사")
		private String siteName;

		@Schema(description = "위도", example = "37.36633726")
		private BigDecimal latitude;

		@Schema(description = "경도", example = "127.10661717")
		private BigDecimal longitude;

		@Schema(description = "지번 주소", example = "경기도 성남시 분당구 정자동 25-1")
		private String jibunAddress;

		@Schema(description = "도로명 주소", example = "경기도 성남시 분당구 성남대로343번길 9")
		private String roadAddress;

		@Schema(description = "사업장 유형", example = "공장")
		private String siteType;
	}
}
