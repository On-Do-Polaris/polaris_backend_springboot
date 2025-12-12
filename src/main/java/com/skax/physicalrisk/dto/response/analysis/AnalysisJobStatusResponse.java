package com.skax.physicalrisk.dto.response.analysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 분석 작업 상태 응답 DTO (v0.2 간소화)
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "분석 작업 상태")
public class AnalysisJobStatusResponse {

	@Schema(description = "작업 상태 (ing: 분석 중, done: 분석 완료)", example = "ing", allowableValues = {"ing", "done"})
	private String status;
}
