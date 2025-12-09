package com.skax.physicalrisk.dto.response.recommendation;

import com.skax.physicalrisk.domain.batch.entity.BatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 후보지 추천 결과 조회 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "후보지 추천 결과 조회 응답")
public class SiteRecommendationResultResponse {

	@Schema(description = "배치 작업 ID", example = "123e4567-e89b-12d3-a456-426614174000")
	private UUID batchJobId;

	@Schema(description = "작업 이름", example = "서울 지역 후보지 추천")
	private String jobName;

	@Schema(description = "작업 상태", example = "completed")
	private BatchStatus status;

	@Schema(description = "작업 시작 시간", example = "2025-12-08T12:00:00")
	private LocalDateTime startedAt;

	@Schema(description = "작업 완료 시간", example = "2025-12-08T12:30:00")
	private LocalDateTime completedAt;

	@Schema(description = "추천 후보지 목록 (위험도 낮은 순)")
	private List<RecommendationItem> recommendations;

	@Schema(description = "총 후보지 수", example = "10")
	private Integer totalCandidates;

	@Schema(description = "오류 메시지")
	private String errorMessage;
}
