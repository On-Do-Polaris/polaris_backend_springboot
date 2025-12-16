package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.response.ErrorResponse;
import com.skax.physicalrisk.dto.request.report.ReportDataRequest;
import com.skax.physicalrisk.dto.response.report.new_structure.ReportResponse;
import io.swagger.v3.oas.annotations.Parameter;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import com.skax.physicalrisk.exception.UnauthorizedException;
import com.skax.physicalrisk.service.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 리포트 컨트롤러 (v0.2)
 *
 * 최종 수정일: 2025-12-16
 * 파일 버전: v03
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
	 * @return 통합 리포트 내용 (TCFD Report Structure)
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 */
	@Operation(
		summary = "통합 리포트 조회",
		description = "보고서 내용을 받아오는 엔드포인트.\nTCFD 기반의 구조화된 리포트 데이터를 반환한다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "통합 리포트 내용",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ReportResponse.class)
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
		responseCode = "500",
		description = "서버 내부 오류",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = "{\"result\": \"error\", \"message\": \"서버 내부 오류가 발생했습니다.\", \"errorCode\": \"INTERNAL_SERVER_ERROR\", \"timestamp\": \"2025-12-12T16:30:00\"}"
			)
		)
	)
	@GetMapping
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<ReportResponse>> getReport() {
		log.info("GET /api/report");
		ReportResponse response = reportService.getReport();
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success(response));
	}

	/**
	 * 리포트 추가 데이터 등록 (v0.2 신규)
	 *
	 * POST /api/report/data
	 *
	 * @param request 리포트 추가 데이터 요청 (siteId, data 객체)
	 * @param file    데이터 파일
	 * @return 빈 응답 (성공)
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 * @throws ResourceNotFoundException 사업장을 찾을 수 없는 경우 (404)
	 */
	@Operation(
		summary = "보고서 추가 데이터 등록",
		description = "보고서 작성을 위해 추가 데이터를 등록하는 엔드포인트.\n사업장 ID와 데이터 파일을 multipart/form-data로 전송한다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "데이터 파일 등록 결과 반환",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(value = "{\"result\": \"success\", \"message\": \"리포트 데이터가 등록되었습니다.\"}")
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
	@ApiResponse(
		responseCode = "500",
		description = "서버 내부 오류",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = "{\"result\": \"error\", \"message\": \"서버 내부 오류가 발생했습니다.\", \"errorCode\": \"INTERNAL_SERVER_ERROR\", \"timestamp\": \"2025-12-12T16:30:00\"}"
			)
		)
	)
	@PostMapping(value = "/data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<Void>> registerReportData(
		@Parameter(
			description = "사업장 ID",
			required = true,
			example = "550e8400-e29b-41d4-a716-446655440000"
		)
		@RequestParam(value = "siteId", required = true) UUID siteId,
		@Parameter(
			description = "업로드할 데이터 파일 (.xlsx, .xls, .csv)",
			required = true
		)
		@RequestPart(value = "file", required = true) MultipartFile file
	) {
		log.info("POST /api/report/data - siteId: {}, fileName: {}", siteId, file.getOriginalFilename());

		// DTO 객체 생성
		ReportDataRequest request = ReportDataRequest.builder()
			.siteId(siteId)
			.build();

		reportService.registerReportData(request, file);
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success("리포트 데이터가 등록되었습니다."));
	}
}