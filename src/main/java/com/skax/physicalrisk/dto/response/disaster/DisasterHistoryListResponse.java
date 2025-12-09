package com.skax.physicalrisk.dto.response.disaster;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 재해 이력 목록 조회 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "재해 이력 목록 조회 응답")
public class DisasterHistoryListResponse {

	@Schema(description = "재해 이력 목록")
	private List<DisasterHistoryItem> items;

	@Schema(description = "총 개수", example = "42")
	private Integer total;

	@Schema(description = "페이지 번호 (1부터 시작)", example = "1")
	private Integer page;

	@Schema(description = "페이지당 개수", example = "20")
	private Integer pageSize;
}
