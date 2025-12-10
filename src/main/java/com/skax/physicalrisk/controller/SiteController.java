package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.site.CreateSiteRequest;
import com.skax.physicalrisk.dto.request.site.UpdateSiteRequest;
import com.skax.physicalrisk.dto.response.site.SiteResponse;
import com.skax.physicalrisk.service.site.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 사업장 컨트롤러
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

	private final SiteService siteService;

	/**
	 * 사업장 목록 조회
	 *
	 * @return 사업장 목록
	 */
	@GetMapping
	public ResponseEntity<SiteResponse> getSites() {
		log.info("GET /api/sites - Fetching all sites");
		SiteResponse response = siteService.getSites();
		return ResponseEntity.ok(response);
	}

	/**
	 * 사업장 생성
	 *
	 * @param request 생성 요청
	 * @return 생성된 사업장 정보
	 */
	@PostMapping
	public ResponseEntity<SiteResponse.SiteInfo> createSite(@Valid @RequestBody CreateSiteRequest request) {
		log.info("POST /api/sites - Name: {}", request.getName());
		SiteResponse.SiteInfo response = siteService.createSite(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 사업장 수정
	 *
	 * @param siteId  사업장 ID (쿼리 파라미터)
	 * @param request 수정 요청
	 * @return 수정된 사업장 정보
	 */
	@PatchMapping
	public ResponseEntity<SiteResponse.SiteInfo> updateSite(
		@RequestParam UUID siteId,
		@RequestBody UpdateSiteRequest request
	) {
		log.info("PATCH /api/sites?siteId={} - Updating site", siteId);
		SiteResponse.SiteInfo response = siteService.updateSite(siteId, request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 사업장 삭제
	 *
	 * @param siteId 사업장 ID (쿼리 파라미터)
	 * @return 성공 메시지
	 */
	@DeleteMapping
	public ResponseEntity<Map<String, String>> deleteSite(@RequestParam UUID siteId) {
		log.info("DELETE /api/sites?siteId={} - Deleting site", siteId);
		siteService.deleteSite(siteId);
		return ResponseEntity.ok(Map.of("message", "사업장이 삭제되었습니다"));
	}
}
