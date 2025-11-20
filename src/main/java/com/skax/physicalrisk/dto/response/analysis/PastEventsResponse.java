package com.skax.physicalrisk.dto.response.analysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 과거 재난 이력 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "과거 재난 이력")
public class PastEventsResponse {

	@Schema(description = "사업장 ID")
	private UUID siteId;

	@Schema(description = "사업장 이름", example = "서울 본사")
	private String siteName;

	@Schema(description = "재난별 이력")
	private List<DisasterEvent> disasters;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "재난 이벤트")
	public static class DisasterEvent {
		@Schema(description = "재난 유형", example = "폭염")
		private String disasterType;

		@Schema(description = "발생 연도", example = "2023")
		private Integer year;

		@Schema(description = "주의 일수", example = "15")
		private Integer warningDays;

		@Schema(description = "심각 일수", example = "5")
		private Integer severeDays;

		@Schema(description = "통합 상태", example = "심각", allowableValues = {"경미", "주의", "심각"})
		private String overallStatus;
	}
}
