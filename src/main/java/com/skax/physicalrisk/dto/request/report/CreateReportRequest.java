package com.skax.physicalrisk.dto.request.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 리포트 생성 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리포트 생성 요청")
public class CreateReportRequest {

	@Schema(description = "사업장 ID (null이면 전체 사업장 리포트)", example = "550e8400-e29b-41d4-a716-446655440000")
	private UUID siteId;
}
