package com.skax.physicalrisk.dto.response.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AAL 리스크 값 응답 DTO
 *
 * FastAPI 재해 종류 매핑:
 * - "가뭄" → "가뭄"
 * - "한파" → "극심한 저온"
 * - "폭염" → "극심한 고온"
 * - "내륙침수" → "하천 홍수"
 * - "해안침수" → "해수면 상승"
 * - "태풍" → "태풍"
 * - "도시침수" → "도시 홍수"
 * - "물부족" → "물 부족"
 * - "산불" → "산불"
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

    @Schema(description = "기간", example = "long", allowableValues = {"short", "mid", "long"})
    private String term;

    @Schema(description = "위험 유형", example = "극심한 고온")
    private String hazardType;

    @Schema(description = "시나리오 1 (SSP1-2.6) AAL 값 (연도별)")
    private Map<String, Double> scenarios1;

    @Schema(description = "시나리오 2 (SSP2-4.5) AAL 값 (연도별)")
    private Map<String, Double> scenarios2;

    @Schema(description = "시나리오 3 (SSP3-7.0) AAL 값 (연도별)")
    private Map<String, Double> scenarios3;

    @Schema(description = "시나리오 4 (SSP5-8.5) AAL 값 (연도별)")
    private Map<String, Double> scenarios4;

    @Schema(description = "재무 영향 발생 근거", example = "태풍으로 인한 시설 피해 복구 비용, 생산 중단에 따른 매출 손실")
    private String reason;

    /**
     * FastAPI 응답 구조를 직접 매핑하는 내부 DTO (scenarios 배열)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FastApiResponse {
        private List<ScenarioData> scenarios;
        private String reason;
    }

    /**
     * 각 시나리오 데이터
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScenarioData {
        private String scenario;  // "SSP1-2.6", "SSP2-4.5", "SSP3-7.0", "SSP5-8.5"

        @JsonProperty("riskType")
        private String riskType;  // FastAPI 재해 종류: "폭염", "한파", "가뭄", "내륙침수", "해안침수", "태풍", "도시침수", "물부족", "산불"

        @JsonProperty("shortTerm")
        private Map<String, Double> shortTerm;  // point1 (단기: 2026년)

        @JsonProperty("midTerm")
        private Map<String, Double> midTerm;  // point1~5 (중기: 2026~2030년)

        @JsonProperty("longTerm")
        private Map<String, Double> longTerm;  // point1~4 (장기: 2020s~2050s)
    }
}