package com.skax.physicalrisk.dto.response.recommendation;

import com.skax.physicalrisk.domain.batch.entity.BatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 배치 작업 진행 상황 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "배치 작업 진행 상황 응답")
public class BatchProgressResponse {

	@Schema(description = "배치 작업 ID", example = "123e4567-e89b-12d3-a456-426614174000")
	private UUID batchJobId;

	@Schema(description = "작업 이름", example = "서울 지역 후보지 추천")
	private String jobName;

	@Schema(description = "작업 상태", example = "running")
	private BatchStatus status;

	@Schema(description = "진행률 (%)", example = "45")
	private Integer progressPercentage;

	@Schema(description = "처리된 항목 수", example = "45")
	private Integer processedItems;

	@Schema(description = "총 항목 수", example = "100")
	private Integer totalItems;

	@Schema(description = "작업 시작 시간", example = "2025-12-08T12:00:00")
	private LocalDateTime startedAt;

	@Schema(description = "작업 완료 시간", example = "2025-12-08T12:30:00")
	private LocalDateTime completedAt;

	@Schema(description = "오류 메시지")
	private String errorMessage;

	@Schema(description = "추가 메타데이터 (JSONB)", example = "{\"estimated_time_remaining\": \"15분\"}")
	private Map<String, Object> metadata;
}
