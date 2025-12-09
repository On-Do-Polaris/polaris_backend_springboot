package com.skax.physicalrisk.dto.request.site;

import com.skax.physicalrisk.domain.site.entity.DataCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 추가 데이터 업로드 요청 DTO
 *
 * ERD v04 기준 - site_additional_data 테이블
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "추가 데이터 업로드 요청")
public class AdditionalDataInput {

	@Schema(description = "데이터 카테고리", example = "building", required = true)
	@NotNull(message = "데이터 카테고리는 필수입니다")
	private DataCategory dataCategory;

	@Schema(description = "원문 텍스트", example = "본 사업장은 2020년 건설되었으며...")
	private String rawText;

	@Schema(description = "정형화된 데이터 (JSONB)", example = "{\"building_age\": 5, \"building_type\": \"철근콘크리트\"}")
	private Map<String, Object> structuredData;

	@Schema(description = "파일명", example = "building_spec.pdf")
	private String fileName;

	@Schema(description = "파일 S3 키", example = "sites/uuid/building_spec.pdf")
	private String fileS3Key;

	@Schema(description = "파일 크기 (bytes)", example = "1024000")
	private Long fileSize;

	@Schema(description = "파일 MIME 타입", example = "application/pdf")
	private String fileMimeType;

	@Schema(description = "메타데이터 (JSONB)", example = "{\"source\": \"manual\"}")
	private Map<String, Object> metadata;

	@Schema(description = "만료 일시", example = "2026-12-31T23:59:59")
	private LocalDateTime expiresAt;
}
