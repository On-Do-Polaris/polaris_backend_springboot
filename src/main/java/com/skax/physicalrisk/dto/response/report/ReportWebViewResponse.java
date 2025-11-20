package com.skax.physicalrisk.dto.response.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 웹 리포트 뷰 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "웹 리포트 뷰")
public class ReportWebViewResponse {

	@Schema(description = "사업장 ID (전체 리포트인 경우 null)")
	private UUID siteId;

	@Schema(description = "리포트 페이지 이미지 목록")
	private List<ReportPage> pages;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "리포트 페이지")
	public static class ReportPage {
		@Schema(description = "페이지 번호", example = "1")
		private Integer pageNumber;

		@Schema(description = "이미지 URL", example = "https://cdn.example.com/reports/page1.png")
		private String imageUrl;

		@Schema(description = "페이지 제목", example = "리스크 개요")
		private String title;
	}
}
