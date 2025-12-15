package com.skax.physicalrisk.dto.request.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 리포트 데이터 등록 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리포트 데이터 등록 요청")
public class ReportDataRequest {

	@Schema(description = "사업장 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "사업장 ID는 필수입니다.")
	private UUID siteId;
}
