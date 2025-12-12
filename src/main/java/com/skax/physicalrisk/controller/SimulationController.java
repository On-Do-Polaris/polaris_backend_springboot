package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.simulation.ClimateSimulationRequest;
import com.skax.physicalrisk.dto.request.simulation.RelocationSimulationRequest;
import com.skax.physicalrisk.dto.response.ErrorResponse;
import com.skax.physicalrisk.dto.response.simulation.ClimateSimulationResponse;
import com.skax.physicalrisk.dto.response.simulation.RelocationSimulationResponse;
import com.skax.physicalrisk.service.simulation.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 시뮬레이션 컨트롤러
 *
 * FastAPI를 통한 기후 시뮬레이션 및 사업장 이전 분석
 *
 * 최종 수정일: 2025-11-20
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@Tag(name = "시뮬레이션", description = "기후 시뮬레이션 및 사업장 이전 분석 API")
public class SimulationController {

	private final SimulationService simulationService;

	/**
	 * 위치 시뮬레이션 후보지 조회
	 *
	 * GET /api/simulation/location/recommendation
	 *
	 * @param siteId 사업장 ID
	 * @return 추천 후보지 3개 및 리스크 정보
	 */
	@Operation(
		summary = "위치 시뮬레이션 후보지 조회",
		description = "특정 사업장과 같은 유형의 추천 후보지 상위 3개를 조회합니다"
	)
	@ApiResponse(
		responseCode = "200",
		description = "추천 후보지 3개와 각 후보지의 리스크 정보",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = com.skax.physicalrisk.dto.response.simulation.LocationRecommendationResponse.class),
			examples = @ExampleObject(
				value = "{\"site\":{\"siteId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"candidate1\":{\"candidateId\":\"a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6\",\"candidateName\":\"세종 데이터 센터 부지\",\"latitude\":36.5040736,\"longitude\":127.2494855,\"jibunAddress\":\"세종특별자치시 보람동 660\",\"roadAddress\":\"세종특별자치시 한누리대로 2130 (보람동)\",\"riskscore\":70,\"aalscore\":20,\"physical-risk-scores\":{\"extreme_heat\":10,\"extreme_cold\":20,\"river_flood\":30},\"aal-scores\":{\"extreme_heat\":9,\"extreme_cold\":10,\"river_flood\":11},\"pros\":\"홍수 위험 62% 감소한다\",\"cons\":\"초기 구축 비용 증가한다\"},\"candidate2\":{\"candidateId\":\"b2c3d4e5-f6a7-b8c9-d0e1-f2a3b4c5d6e7\",\"candidateName\":\"부산 데이터 센터 부지\",\"latitude\":35.1795543,\"longitude\":129.0756416,\"jibunAddress\":\"부산광역시 연제구 연산동 1000\",\"roadAddress\":\"부산광역시 연제구 중앙대로 1001\",\"riskscore\":65,\"aalscore\":18,\"physical-risk-scores\":{\"extreme_heat\":15,\"extreme_cold\":10,\"river_flood\":25},\"aal-scores\":{\"extreme_heat\":8,\"extreme_cold\":9,\"river_flood\":12},\"pros\":\"해수면 상승 영향 적음\",\"cons\":\"태풍 위험도 높음\"},\"candidate3\":{\"candidateId\":\"c3d4e5f6-a7b8-c9d0-e1f2-a3b4c5d6e7f8\",\"candidateName\":\"춘천 데이터 센터 부지\",\"latitude\":37.8813,\"longitude\":127.7298,\"jibunAddress\":\"강원도 춘천시 동면 123\",\"roadAddress\":\"강원도 춘천시 동면 순환대로 1234\",\"riskscore\":50,\"aalscore\":10,\"physical-risk-scores\":{\"extreme_heat\":5,\"extreme_cold\":30,\"river_flood\":10},\"aal-scores\":{\"extreme_heat\":4,\"extreme_cold\":15,\"river_flood\":5},\"pros\":\"대부분의 기후 리스크 낮음\",\"cons\":\"겨울철 한파 리스크 높음\"}}}"
			)
		)
	)
	@ApiResponse(
		responseCode = "401",
		description = "인증되지 않은 사용자",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"인증되지 않은 사용자입니다.\", \"errorCode\": \"UNAUTHORIZED\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@ApiResponse(
		responseCode = "404",
		description = "사업장을 찾을 수 없음",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"사업장을 찾을 수 없습니다.\", \"errorCode\": \"SITE_NOT_FOUND\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@GetMapping("/location/recommendation")
	public ResponseEntity<com.skax.physicalrisk.dto.response.simulation.LocationRecommendationResponse> getLocationRecommendation(
		@Parameter(description = "사업장 ID", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		@RequestParam String siteId
	) {
		log.info("GET /api/simulation/location/recommendation - siteId: {}", siteId);
		com.skax.physicalrisk.dto.response.simulation.LocationRecommendationResponse response = simulationService.getLocationRecommendation(siteId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 위치 시뮬레이션 비교
	 *
	 * POST /api/simulation/location/compare
	 *
	 * @param request 비교 시뮬레이션 요청
	 * @return 비교 결과
	 */
	@Operation(
		summary = "위치 시뮬레이션 비교",
		description = "특정 주소를 입력하면 특정 사업장과 비교하는 시뮬레이션.\n새 후보지는 특정 사업장과 같은 유형이라고 가정한다."
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "현재 사업장 ID와 비교할 후보지 정보",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = RelocationSimulationRequest.class),
			examples = @ExampleObject(
				value = "{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"candidate\": {\"latitude\": 36.5040736, \"longitude\": 127.2494855, \"jibunAddress\": \"세종특별자치시 보람동 660\", \"roadAddress\": \"세종특별자치시 한누리대로 2130 (보람동)\"}}"
			)
		)
	)
	@ApiResponse(
		responseCode = "200",
		description = "비교 대상 후보지의 리스크 정보",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = RelocationSimulationResponse.class),
			examples = @ExampleObject(
				value = "{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"candidate\": {\"candidateId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"latitude\": 36.5040736, \"longitude\": 127.2494855, \"jibunAddress\": \"세종특별자치시 보람동 660\", \"roadAddress\": \"세종특별자치시 한누리대로 2130 (보람동)\", \"riskscore\": 70, \"aalscore\": 20, \"physical-risk-scores\": {\"extreme_heat\": 10, \"extreme_cold\": 20, \"river_flood\": 30, \"urban_flood\": 40, \"drought\": 50, \"water_stress\": 60, \"sea_level_rise\": 50, \"typhoon\": 70, \"wildfire\": 60}, \"aal-scores\": {\"extreme_heat\": 9, \"extreme_cold\": 10, \"river_flood\": 11, \"urban_flood\": 12, \"drought\": 13, \"water_stress\": 14, \"sea_level_rise\": 15, \"typhoon\": 17, \"wildfire\": 16}, \"pros\": \"홍수 위험 62% 감소한다\", \"cons\": \"초기 구축 비용 증가한다\"}}"
			)
		)
	)
	@ApiResponse(
		responseCode = "401",
		description = "인증되지 않은 사용자",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"인증되지 않은 사용자입니다.\", \"errorCode\": \"UNAUTHORIZED\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@ApiResponse(
		responseCode = "404",
		description = "사업장을 찾을 수 없음",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"사업장을 찾을 수 없습니다.\", \"errorCode\": \"SITE_NOT_FOUND\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@PostMapping("/location/compare")
	public ResponseEntity<RelocationSimulationResponse> compareLocation(
		@Valid @RequestBody RelocationSimulationRequest request
	) {
		log.info("POST /api/simulation/location/compare");
		RelocationSimulationResponse response = simulationService.compareLocation(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 기후 시뮬레이션
	 *
	 * POST /api/simulation/climate
	 *
	 * @param request 기후 시뮬레이션 요청
	 * @return 시뮬레이션 결과
	 */
	@Operation(
		summary = "기후 시뮬레이션",
		description = "SSP 시나리오와 기후 변수에 따라 연도별 모든 사업장 값을 반환한다.\n대응 방안은 현재 API에 존재하지 않으며, 추후 필요 시 필드 추가가 필요하다."
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "시뮬레이션 시나리오와 위험 유형",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ClimateSimulationRequest.class),
			examples = @ExampleObject(
				value = "{\"scenario\": \"SSP2-4.5\", \"hazardType\": \"극심한 고온\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "200",
		description = "시나리오별, 연도별 사업장 기후 데이터",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ClimateSimulationResponse.class),
			examples = @ExampleObject(
				value = "{\"scenario\": \"SSP2-4.5\", \"riskType\": \"극심한 고온\", \"yearlyData\": [{\"year\": 2030, \"nationalAverageTemperature\": 14.5, \"sites\": [{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"siteName\": \"sk u 타워\", \"Temperature\": 15, \"riskincrease\": 15.2}, {\"siteId\": \"3fa96f64-5789-6859-b3fc-2c963f23dhi6\", \"siteName\": \"판교 데이터 센터\", \"Temperature\": 14.8, \"riskincrease\": 18}]}, {\"year\": 2040, \"nationalAverageTemperature\": 16.5, \"sites\": [{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"siteName\": \"sk u 타워\", \"Temperature\": 17, \"riskincrease\": 17.2}, {\"siteId\": \"3fa96f64-5789-6859-b3fc-2c963f23dhi6\", \"siteName\": \"판교 데이터 센터\", \"Temperature\": 15.8, \"riskincrease\": 19}]}]}"
			)
		)
	)
	@ApiResponse(
		responseCode = "401",
		description = "인증되지 않은 사용자",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"인증되지 않은 사용자입니다.\", \"errorCode\": \"UNAUTHORIZED\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@PostMapping("/climate")
	public ResponseEntity<ClimateSimulationResponse> runClimateSimulation(
		@Valid @RequestBody ClimateSimulationRequest request
	) {
		log.info("POST /api/simulation/climate");
		ClimateSimulationResponse response = simulationService.runClimateSimulation(request);
		return ResponseEntity.ok(response);
	}
}
