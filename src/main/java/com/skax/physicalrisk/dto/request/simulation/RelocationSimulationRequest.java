package com.skax.physicalrisk.dto.request.simulation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 사업장 이전 시뮬레이션 요청 DTO
 *
 * @author SKAX Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사업장 이전 시뮬레이션 요청")
public class RelocationSimulationRequest {

    @Schema(description = "현재 사업장 ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull(message = "사업장 ID는 필수입니다.")
    private UUID siteId;

    @Schema(description = "비교할 후보지 정보")
    @NotNull
    @Valid
    private Candidate candidate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "후보지 정보")
    public static class Candidate {
        @Schema(description = "후보지 위도", required = true, example = "36.5040736")
        @NotNull(message = "위도는 필수입니다.")
        private BigDecimal latitude;

        @Schema(description = "후보지 경도", required = true, example = "127.2494855")
        @NotNull(message = "경도는 필수입니다.")
        private BigDecimal longitude;

        @Schema(description = "후보지 지번 주소", example = "세종특별자치시 보람동 660")
        private String jibunAddress;

        @Schema(description = "후보지 도로명 주소", example = "세종특별자치시 한누리대로 2130 (보람동)")
        private String roadAddress;
    }
}