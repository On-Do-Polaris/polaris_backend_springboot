package com.skax.physicalrisk.dto.response.analysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * AAL 리스크 값 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AAL 시나리오별 값")
public class FinancialImpactResponse {

    @Schema(description = "사업장 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID siteId;

    @Schema(description = "기간", example = "long")
    private String term;

    @Schema(description = "위험 유형", example = "극심한 고온")
    private String hazardType;

    @Schema(description = "시나리오 1 점수")
    private Map<String, Integer> scenarios1;

    @Schema(description = "시나리오 2 점수")
    private Map<String, Integer> scenarios2;

    @Schema(description = "시나리오 3 점수")
    private Map<String, Integer> scenarios3;

    @Schema(description = "시나리오 4 점수")
    private Map<String, Integer> scenarios4;

    @Schema(description = "재무 영향 발생 근거", example = "태풍으로 인한 시설 피해 복구 비용, 생산 중단에 따른 매출 손실")
    private String reason;
}