package com.skax.physicalrisk.dto.response.site;

import com.skax.physicalrisk.domain.site.entity.DataCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 추가 데이터 업로드 응답 DTO
 *
 * ERD v04 기준 - site_additional_data 테이블
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "추가 데이터 업로드 응답")
public class AdditionalDataUploadResponse {

	@Schema(description = "레코드 ID", example = "123e4567-e89b-12d3-a456-426614174000")
	private UUID id;

	@Schema(description = "사업장 ID", example = "123e4567-e89b-12d3-a456-426614174000")
	private UUID siteId;

	@Schema(description = "데이터 카테고리", example = "building")
	private DataCategory dataCategory;

	@Schema(description = "상태", example = "uploaded")
	private String status;

	@Schema(description = "업로드 일시", example = "2025-12-08T12:00:00")
	private LocalDateTime uploadedAt;
}
