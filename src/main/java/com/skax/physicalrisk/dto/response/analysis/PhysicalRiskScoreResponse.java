package com.skax.physicalrisk.dto.response.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * 물리적 리스크 시나리오별 값 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "물리적 리스크 시나리오별 값")
public class PhysicalRiskScoreResponse {

    @Schema(description = "사업장 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID siteId;

    @Schema(description = "기간", example = "long", allowableValues = {"short", "mid", "long"})
    private String term;

    @Schema(description = "위험 유형", example = "극심한 고온")
    private String hazardType;

    @Schema(description = "시나리오 1 점수 상세")
    private Map<String, RiskScoreDetail> scenarios1;

    @Schema(description = "시나리오 2 점수 상세")
    private Map<String, RiskScoreDetail> scenarios2;

    @Schema(description = "시나리오 3 점수 상세")
    private Map<String, RiskScoreDetail> scenarios3;

    @Schema(description = "시나리오 4 점수 상세")
    private Map<String, RiskScoreDetail> scenarios4;

    @JsonProperty("Strategy")
    @Schema(description = "대응 방안", example = "냉각 시스템 강화 및 단열재 보강")
    private String Strategy;



    /**
     * 리스크 점수 상세 구조체 (Total, H, E, V)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "물리적 리스크 상세 점수 (Total, H, E, V)")
    public static class RiskScoreDetail {
        
        @Schema(description = "종합 점수 (Total)", example = "50")
        private Double total; // 소수점 계산이 필요하다면 Double, 정수면 Integer

        @Schema(description = "Hazard 점수", example = "10")
        private Double h;

        @Schema(description = "Exposure 점수", example = "20")
        private Double e;

        @Schema(description = "Vulnerability 점수", example = "20")
        private Double v;
    }
}