package com.skax.physicalrisk.dto.response.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PDF 리포트 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PDF 리포트")
public class ReportPdfResponse {

	@Schema(description = "PDF 다운로드 URL", example = "https://cdn.example.com/reports/report-20250120.pdf")
	private String downloadUrl;

	@Schema(description = "파일 크기 (bytes)", example = "1048576")
	private Long fileSize;

	@Schema(description = "만료 시간")
	private LocalDateTime expiresAt;
}
