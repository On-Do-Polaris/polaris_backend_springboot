package com.skax.physicalrisk.dto.response.site;

import com.skax.physicalrisk.domain.site.entity.DataCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 추가 데이터 조회 응답 DTO
 *
 * ERD v04 기준 - site_additional_data 테이블의 모든 필드 포함
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "추가 데이터 조회 응답")
public class AdditionalDataGetResponse {

	@Schema(description = "레코드 ID", example = "123e4567-e89b-12d3-a456-426614174000")
	private UUID id;

	@Schema(description = "사업장 ID", example = "123e4567-e89b-12d3-a456-426614174000")
	private UUID siteId;

	@Schema(description = "데이터 카테고리", example = "building")
	private DataCategory dataCategory;

	@Schema(description = "원문 텍스트")
	private String rawText;

	@Schema(description = "정형화된 데이터 (JSONB)")
	private Map<String, Object> structuredData;

	@Schema(description = "파일명", example = "building_spec.pdf")
	private String fileName;

	@Schema(description = "파일 S3 키", example = "sites/uuid/building_spec.pdf")
	private String fileS3Key;

	@Schema(description = "파일 크기 (bytes)", example = "1024000")
	private Long fileSize;

	@Schema(description = "파일 MIME 타입", example = "application/pdf")
	private String fileMimeType;

	@Schema(description = "메타데이터 (JSONB)")
	private Map<String, Object> metadata;

	@Schema(description = "업로드한 사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
	private UUID uploadedBy;

	@Schema(description = "업로드 일시", example = "2025-12-08T12:00:00")
	private LocalDateTime uploadedAt;

	@Schema(description = "만료 일시", example = "2026-12-31T23:59:59")
	private LocalDateTime expiresAt;
}
