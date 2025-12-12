package com.skax.physicalrisk.dto.response.simulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * 위치 시뮬레이션 비교 응답 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "위치 시뮬레이션 비교 결과")
public class RelocationSimulationResponse {

    @Schema(description = "비교의 기준이 된 현재 사업장 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID siteId;

    @Schema(description = "비교 대상 후보지 정보")
    private Candidate candidate;

    @Schema(description = "현재 사업장 위치 정보")
    private LocationData currentLocation;

    @Schema(description = "새 후보지 위치 정보")
    private LocationData newLocation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "후보지 상세 정보")
    public static class Candidate {

        @Schema(description = "후보지 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        private UUID candidateId;

        @Schema(description = "위도", example = "36.5040736")
        private BigDecimal latitude;

        @Schema(description = "경도", example = "127.2494855")
        private BigDecimal longitude;

        @Schema(description = "지번 주소", example = "세종특별자치시 보람동 660")
        private String jibunAddress;

        @Schema(description = "도로명 주소", example = "세종특별자치시 한누리대로 2130 (보람동)")
        private String roadAddress;

        @Schema(description = "통합 리스크 점수", example = "70")
        private Integer riskscore;

        @Schema(description = "통합 AAL 점수", example = "20")
        private Integer aalscore;

        @JsonProperty("physical-risk-scores")
        @Schema(description = "재해 유형별 물리적 리스크 점수")
        private Map<String, Integer> physicalRiskScores;

        @JsonProperty("aal-scores")
        @Schema(description = "재해 유형별 AAL 점수")
        private Map<String, Integer> aalScores;

        @Schema(description = "장점", example = "홍수 위험 62% 감소한다")
        private String pros;

        @Schema(description = "단점", example = "초기 구축 비용 증가한다")
        private String cons;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "위치 상세 정보")
    public static class LocationData {
        @Schema(description = "리스크 목록")
        private java.util.List<RiskData> risks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리스크 상세 정보")
    public static class RiskData {
        @Schema(description = "리스크 유형", example = "flood")
        private String riskType;

        @Schema(description = "물리적 리스크 점수", example = "75.5")
        private Double physicalRiskScore;

        @Schema(description = "AAL (연평균손실) 점수", example = "20.5")
        private Double aal;
    }
}