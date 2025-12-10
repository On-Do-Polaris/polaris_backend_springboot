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
	 * 통합 리포트 조회
	 *
	 * GET /api/report
	 *
	 * @return 통합 리포트 내용 (ceosummry, Governance, strategy, riskmanagement, goal)
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 */
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
	@PostMapping("/data")
	public ResponseEntity<Map<String, Object>> registerReportData(
		@Valid @RequestBody Map<String, Object> request
	) {
		log.info("POST /api/report/data - request: {}", request);
		reportService.registerReportData(request);
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}
}
