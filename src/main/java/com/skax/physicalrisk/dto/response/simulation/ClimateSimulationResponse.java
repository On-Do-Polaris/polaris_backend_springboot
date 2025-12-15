package com.skax.physicalrisk.dto.response.simulation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 기후 시뮬레이션 응답 DTO
 * 행정구역별 기후 점수 및 사업장별 AAL 데이터 제공
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기후 시뮬레이션 결과 (행정구역별 점수 및 사업장별 AAL)")
public class ClimateSimulationResponse {

    @Schema(description = "SSP 시나리오", example = "SSP2-4.5")
    private String scenario;

    @Schema(description = "리스크 유형 (Hazard Type)", example = "극심한 고온")
    private String hazardType;

    @Schema(description = "행정구역별 기후 점수 (Key: 행정구역코드, Value: {연도: 점수})", 
            example = "{\"11010\": {\"2025\": 45.2, \"2100\": 89.3}}")
    private Map<String, Map<String, Double>> regionScores;

    @Schema(description = "사업장별 시뮬레이션 데이터 목록")
    private List<SiteSimulationData> sites;

    /**
     * 사업장별 데이터 내부 클래스
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사업장 시뮬레이션 상세 정보")
    public static class SiteSimulationData {

        @Schema(description = "사업장 고유 ID", example = "4b5be9aa-c228-4a13-b0c5-0d98deb51424")
        private UUID siteId;

        @Schema(description = "사업장 이름", example = "SK ATS")
        private String siteName;

        @Schema(description = "사업장 위치 행정구역 코드 (5자리)", example = "11010")
        private String regionCode;

        @Schema(description = "연도별 AAL(Annual Average Loss) 값 (Key: 연도, Value: 수치)", 
                example = "{\"2025\": 12.5, \"2100\": 46.3}")
        private Map<String, Double> aalByYear;
    }
}