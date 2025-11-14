package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.site.CreateSiteRequest;
import com.skax.physicalrisk.dto.request.site.UpdateSiteRequest;
import com.skax.physicalrisk.dto.response.site.SiteResponse;
import com.skax.physicalrisk.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	 * @param keyword  검색 키워드 (선택)
	 * @param pageable 페이징 정보
	 * @return 사업장 목록
	 */
	@GetMapping
	public ResponseEntity<Page<SiteResponse>> getSites(
		@RequestParam(required = false) String keyword,
		Pageable pageable
	) {
		log.info("GET /api/sites - Keyword: {}, Page: {}", keyword, pageable.getPageNumber());

		Page<SiteResponse> sites = keyword != null && !keyword.isEmpty()
			? siteService.searchSites(keyword, pageable)
			: siteService.getSites(pageable);

		return ResponseEntity.ok(sites);
	}

	/**
	 * 사업장 생성
	 *
	 * @param request 생성 요청
	 * @return 생성된 사업장 정보
	 */
	@PostMapping
	public ResponseEntity<SiteResponse> createSite(@Valid @RequestBody CreateSiteRequest request) {
		log.info("POST /api/sites - Name: {}", request.getName());
		SiteResponse response = siteService.createSite(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 사업장 상세 조회
	 *
	 * @param siteId 사업장 ID
	 * @return 사업장 상세 정보
	 */
	@GetMapping("/{siteId}")
	public ResponseEntity<SiteResponse> getSite(@PathVariable UUID siteId) {
		log.info("GET /api/sites/{} - Fetching site", siteId);
		SiteResponse response = siteService.getSite(siteId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 사업장 수정
	 *
	 * @param siteId  사업장 ID
	 * @param request 수정 요청
	 * @return 수정된 사업장 정보
	 */
	@PatchMapping("/{siteId}")
	public ResponseEntity<SiteResponse> updateSite(
		@PathVariable UUID siteId,
		@RequestBody UpdateSiteRequest request
	) {
		log.info("PATCH /api/sites/{} - Updating site", siteId);
		SiteResponse response = siteService.updateSite(siteId, request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 사업장 삭제
	 *
	 * @param siteId 사업장 ID
	 * @return 성공 메시지
	 */
	@DeleteMapping("/{siteId}")
	public ResponseEntity<Map<String, String>> deleteSite(@PathVariable UUID siteId) {
		log.info("DELETE /api/sites/{} - Deleting site", siteId);
		siteService.deleteSite(siteId);
		return ResponseEntity.ok(Map.of("message", "사업장이 삭제되었습니다"));
	}
}
