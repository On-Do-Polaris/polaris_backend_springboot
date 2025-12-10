package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.report.CreateReportRequest;
import com.skax.physicalrisk.dto.response.report.ReportPdfResponse;
import com.skax.physicalrisk.dto.response.report.ReportWebViewResponse;
import com.skax.physicalrisk.service.report.ReportService;
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
	 * 리포트 생성
	 *
	 * POST /api/report
	 *
	 * @param request 리포트 생성 요청
	 * @return 생성된 리포트 정보
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 * @throws ResourceNotFoundException 사업장을 찾을 수 없는 경우 (404)
	 */
	@PostMapping
	public ResponseEntity<Map<String, Object>> createReport(
		@Valid @RequestBody CreateReportRequest request
	) {
		log.info("POST /api/report - siteId: {}", request.getSiteId());
		Map<String, Object> response = reportService.createReport(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
	@PostMapping("/data")
	public ResponseEntity<Map<String, String>> registerReportData(
		@Valid @RequestBody Map<String, Object> request
	) {
		log.info("POST /api/report/data - request: {}", request);
		reportService.registerReportData(request);
		return ResponseEntity.ok(Map.of("message", "리포트 데이터가 등록되었습니다"));
	}

	/**
	 * 리포트 웹 뷰 조회
	 *
	 * GET /api/reports/web
	 *
	 * @return 웹 뷰 리포트
	 */
	@GetMapping("/web")
	public ResponseEntity<ReportWebViewResponse> getReportWebView() {
		log.info("GET /api/reports/web");
		ReportWebViewResponse response = reportService.getReportWebView();
		return ResponseEntity.ok(response);
	}

	/**
	 * 리포트 PDF 다운로드 정보 조회
	 *
	 * GET /api/reports/pdf
	 *
	 * @return PDF 다운로드 정보
	 */
	@GetMapping("/pdf")
	public ResponseEntity<ReportPdfResponse> getReportPdf() {
		log.info("GET /api/reports/pdf");
		ReportPdfResponse response = reportService.getReportPdf();
		return ResponseEntity.ok(response);
	}

	/**
	 * 리포트 삭제
	 *
	 * DELETE /api/reports
	 *
	 * @return 성공 메시지
	 */
	@DeleteMapping
	public ResponseEntity<Map<String, String>> deleteReport() {
		log.info("DELETE /api/reports");
		reportService.deleteReport();
		return ResponseEntity.ok(Map.of("message", "리포트가 삭제되었습니다"));
	}
}
