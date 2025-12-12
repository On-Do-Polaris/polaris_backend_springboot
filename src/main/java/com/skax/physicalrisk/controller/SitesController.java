package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.response.ErrorResponse;
import com.skax.physicalrisk.dto.response.site.SiteResponse;
import com.skax.physicalrisk.service.site.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 전체 사업장 조회 컨트롤러 (v0.2)
 *
 * 최종 수정일: 2025-12-10
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SitesController {

	private final SiteService siteService;

	/**
	 * 전체 사업장 조회
	 *
	 * GET /api/sites
	 *
	 * @return 전체 사업장 목록
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 */
	@Operation(
		summary = "전체 사업장 조회",
		description = "현재 로그인한 사용자의 전체 사업장 목록을 조회합니다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "전체 사업장 목록 반환",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = SiteResponse.class),
			examples = @ExampleObject(
				value = "{\"sites\": [{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"siteName\": \"sk u 타워\", \"latitude\": 37.36633726, \"longitude\": 127.10661717, \"jibunAddress\": \"경기도 성남시 분당구 정자동 25-1\", \"roadAddress\": \"경기도 성남시 분당구 성남대로343번길 9\", \"siteType\": \"data_center\"}, {\"siteId\": \"3fa96f64-5789-6859-b3fc-2c963f23dhi6\", \"siteName\": \"판교 데이터 센터\", \"latitude\": 37.4003481203, \"longitude\": 127.1049265705, \"jibunAddress\": \"경기도 성남시 분당구 삼평동 612-4 SK 판교캠퍼스 B\", \"roadAddress\": \"경기도 성남시 분당구 판교로255번길 38 (삼평동)\", \"siteType\": \"data_center\"}]}"
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
	public ResponseEntity<SiteResponse> getAllSites() {
		log.info("GET /api/sites - Fetching all sites");
		SiteResponse response = siteService.getSites();
		return ResponseEntity.ok(response);
	}
}
