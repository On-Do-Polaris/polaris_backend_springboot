package com.skax.physicalrisk.dto.response.recommendation;

import com.skax.physicalrisk.domain.batch.entity.BatchStatus;
import com.skax.physicalrisk.domain.batch.entity.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 후보지 추천 배치 시작 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "후보지 추천 배치 시작 응답")
public class SiteRecommendationBatchResponse {

	@Schema(description = "배치 작업 ID", example = "123e4567-e89b-12d3-a456-426614174000")
	private UUID batchJobId;

	@Schema(description = "작업 유형", example = "site_recommendation")
	private JobType jobType;

	@Schema(description = "작업 이름", example = "서울 지역 후보지 추천")
	private String jobName;

	@Schema(description = "작업 상태", example = "queued")
	private BatchStatus status;

	@Schema(description = "작업 시작 시간", example = "2025-12-08T12:00:00")
	private LocalDateTime startedAt;

	@Schema(description = "총 후보지 수", example = "10")
	private Integer totalCandidates;

	@Schema(description = "진행률 (%)", example = "0")
	private Integer progressPercentage;
}
