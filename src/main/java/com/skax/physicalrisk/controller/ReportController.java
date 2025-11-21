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
 * 리포트 컨트롤러
 *
 * 최종 수정일: 2025-11-20
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	/**
	 * 리포트 생성
	 *
	 * POST /api/reports
	 *
	 * @param request 리포트 생성 요청
	 * @return 생성된 리포트 정보
	 */
	@PostMapping
	public ResponseEntity<Map<String, Object>> createReport(
		@Valid @RequestBody CreateReportRequest request
	) {
		log.info("POST /api/reports - siteId: {}", request.getSiteId());
		Map<String, Object> response = reportService.createReport(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
