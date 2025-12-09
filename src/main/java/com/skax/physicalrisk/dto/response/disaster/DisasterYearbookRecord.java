package com.skax.physicalrisk.dto.response.disaster;

import com.skax.physicalrisk.domain.disaster.entity.DisasterSeverity;
import com.skax.physicalrisk.domain.disaster.entity.DisasterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 재해연보 데이터 DTO
 *
 * ERD v04 기준 - api_disaster_yearbook 테이블
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "재해연보 데이터")
public class DisasterYearbookRecord {

	@Schema(description = "재해연보 ID", example = "12345")
	private Integer yearbookId;

	@Schema(description = "연도", example = "2023")
	private Integer year;

	@Schema(description = "행정구역 코드 (NULL=전국 통계)", example = "11110")
	private String adminCode;

	@Schema(description = "재해 유형", example = "TYPHOON")
	private DisasterType disasterType;

	@Schema(description = "태풍 피해액 (억원)", example = "150.5")
	private Double typhoonDamage;

	@Schema(description = "호우 피해액 (억원)", example = "200.0")
	private Double heavyRainDamage;

	@Schema(description = "대설 피해액 (억원)", example = "50.0")
	private Double heavySnowDamage;

	@Schema(description = "강풍 피해액 (억원)", example = "30.0")
	private Double strongWindDamage;

	@Schema(description = "풍랑 피해액 (억원)", example = "20.0")
	private Double windWaveDamage;

	@Schema(description = "지진 피해액 (억원)", example = "0.0")
	private Double earthquakeDamage;

	@Schema(description = "기타 피해액 (억원)", example = "10.0")
	private Double otherDamage;

	@Schema(description = "총 피해액 (억원)", example = "460.5")
	private Double totalDamage;

	@Schema(description = "손실액 (원)", example = "46050000000")
	private Long lossAmountWon;

	@Schema(description = "피해 건물 수", example = "150")
	private Integer affectedBuildings;

	@Schema(description = "피해 인구", example = "5000")
	private Integer affectedPopulation;

	@Schema(description = "데이터 출처", example = "행정안전부")
	private String dataSource;

	@Schema(description = "주요 재해 유형", example = "HEAVY_RAIN")
	private DisasterType majorDisasterType;

	@Schema(description = "피해 수준", example = "SEVERE")
	private DisasterSeverity damageLevel;

	@Schema(description = "캐시된 일시", example = "2025-12-08T12:00:00")
	private LocalDateTime cachedAt;

	@Schema(description = "API 응답 원본 (JSONB)")
	private Map<String, Object> apiResponse;
}
