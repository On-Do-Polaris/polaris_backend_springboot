package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.report.CreateReportRequest;
import com.skax.physicalrisk.dto.response.report.ReportPdfResponse;
import com.skax.physicalrisk.dto.response.report.ReportWebViewResponse;
import com.skax.physicalrisk.service.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 리포트 컨트롤러 (v0.2)
 *
 * 최종 수정일: 2025-12-10
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	/**
	 * 통합 리포트 조회
	 *
	 * GET /api/report
	 *
	 * @return 통합 리포트 내용 (ceosummry, Governance, strategy, riskmanagement, goal)
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 */
	@Operation(
		summary = "통합 리포트 조회",
		description = "보고서 내용을 받아오는 엔드포인트.\nCEO 요약, 거버넌스, 전략, 리스크 관리, 목표 정보를 포함한다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "통합 리포트 내용",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = Map.class),
			examples = @ExampleObject(
				value = "{\"ceosummry\": \"회사는 현재 기후 관련 위험을 면밀히 분석했습니다\", \"Governance\": \"기후 거버넌스는 당사의 지속 가능한 운영과 자산 가치를 극대화하기 위한 필수 요소입니다.\", \"strategy\": \"기후 변화에 대한 포괄적 접근 방식을 통해 우리는 지속 가능한 운영을 도모합니다.\", \"riskmanagement\": \"리스크 관리의 일환으로 당사는 여러 프로세스를 도입했습니다.\", \"goal\": \"현재 기후 리스크로 인해 예상되는 손실과 당사의 목표입니다.\"}"
			)
		)
	)
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	@GetMapping
	public ResponseEntity<Map<String, String>> getReport() {
		log.info("GET /api/report");
		Map<String, String> response = reportService.getReport();
		return ResponseEntity.ok(response);
	}

	/**
	 * 리포트 추가 데이터 등록 (v0.2 신규)
	 *
	 * POST /api/report/data
	 *
	 * @param request 리포트 추가 데이터 요청 (siteId, data 객체)
	 * @return 빈 응답 (성공)
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 * @throws ResourceNotFoundException 사업장을 찾을 수 없는 경우 (404)
	 */
	@Operation(
		summary = "보고서 추가 데이터 등록",
		description = "보고서 작성을 위해 추가 데이터를 등록하는 엔드포인트.\n사업장별로 데이터 파일을 업로드할 수 있다."
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "사업장 ID와 데이터",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = Map.class),
			examples = @ExampleObject(
				value = "{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"data\": \"여기가 데이터 파일\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "200",
		description = "데이터 파일 등록 결과 반환",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = Map.class),
			examples = @ExampleObject(value = "{}")
		)
	)
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	@ApiResponse(responseCode = "404", description = "사업장을 찾을 수 없음")
	@PostMapping("/data")
	public ResponseEntity<Map<String, Object>> registerReportData(
		@Valid @RequestBody Map<String, Object> request
	) {
		log.info("POST /api/report/data - request: {}", request);
		reportService.registerReportData(request);
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}
}
