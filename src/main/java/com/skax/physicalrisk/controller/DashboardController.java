package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 대시보드 컨트롤러
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	/**
	 * 대시보드 요약 정보 조회
	 *
	 * @return 대시보드 요약 정보
	 */
	@GetMapping("/summary")
	public ResponseEntity<Map<String, Object>> getDashboardSummary() {
		log.info("GET /api/dashboard/summary - Fetching dashboard summary");
		Map<String, Object> summary = dashboardService.getDashboardSummary();
		return ResponseEntity.ok(summary);
	}
}
