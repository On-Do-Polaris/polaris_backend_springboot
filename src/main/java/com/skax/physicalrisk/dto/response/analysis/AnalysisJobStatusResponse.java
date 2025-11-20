package com.skax.physicalrisk.dto.response.analysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 분석 작업 상태 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "분석 작업 상태")
public class AnalysisJobStatusResponse {

	@Schema(description = "작업 ID", example = "job-123456")
	private String jobId;

	@Schema(description = "사업장 ID")
	private UUID siteId;

	@Schema(description = "작업 상태", example = "running", allowableValues = {"queued", "running", "completed", "failed"})
	private String status;

	@Schema(description = "현재 처리 중인 노드", example = "physical_risk_score")
	private String currentNode;

	@Schema(description = "에러 정보")
	private ErrorInfo error;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "에러 정보")
	public static class ErrorInfo {
		@Schema(description = "에러 코드", example = "ANALYSIS_FAILED")
		private String code;

		@Schema(description = "에러 메시지", example = "데이터 조회 실패")
		private String message;
	}
}
