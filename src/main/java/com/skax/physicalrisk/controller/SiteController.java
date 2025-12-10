package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.site.CreateSiteRequest;
import com.skax.physicalrisk.dto.request.site.UpdateSiteRequest;
import com.skax.physicalrisk.dto.response.site.SiteResponse;
import com.skax.physicalrisk.service.site.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * 사업장 컨트롤러 (v0.2)
 *
 * 최종 수정일: 2025-12-10
 * 파일 버전: v02
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/site")
@RequiredArgsConstructor
public class SiteController {

	private final SiteService siteService;

	/**
	 * 특정 사업장 조회 (이름으로 검색)
	 *
	 * GET /api/site?siteName={siteName}
	 *
	 * @param siteName 사업장명 (필수)
	 * @return 사업장 정보
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 * @throws ResourceNotFoundException 사업장을 찾을 수 없는 경우 (404)
	 */
	@Operation(
		summary = "특정 사업장 조회",
		description = "사업장 이름으로 특정 사업장의 정보를 조회합니다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "사업장 정보 반환",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = SiteResponse.SiteInfo.class),
			examples = @ExampleObject(
				value = "{\"siteId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"siteName\": \"sk u 타워\", \"latitude\": 37.36633726, \"longitude\": 127.10661717, \"jibunAddress\": \"경기도 성남시 분당구 정자동 25-1\", \"roadAddress\": \"경기도 성남시 분당구 성남대로343번길 9\", \"siteType\": \"data_center\"}"
			)
		)
	)
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	@ApiResponse(responseCode = "404", description = "사업장을 찾을 수 없음")
	@GetMapping
	public ResponseEntity<SiteResponse.SiteInfo> getSite(
		@Parameter(description = "사업장명", required = true, example = "sk u 타워")
		@RequestParam String siteName
	) {
		log.info("GET /api/site?siteName={} - Searching site by name", siteName);
		SiteResponse.SiteInfo response = siteService.getSiteByName(siteName);
		return ResponseEntity.ok(response);
	}

	/**
	 * 사업장 생성
	 *
	 * @param request 생성 요청 (name: 사업장명, latitude: 위도, longitude: 경도, jibunAddress: 지번주소, roadAddress: 도로명주소, type: 사업장 유형)
	 * @return 생성된 사업장 정보 (siteId: 사업장 ID, siteName: 사업장명, latitude: 위도, longitude: 경도, jibunAddress: 지번주소, roadAddress: 도로명주소, siteType: 사업장 유형)
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 * @throws ValidationException 사업장 데이터가 유효하지 않은 경우 (422)
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
	 * @param request 수정 요청 (name: 사업장명, latitude: 위도, longitude: 경도, jibunAddress: 지번주소, roadAddress: 도로명주소, type: 사업장 유형 - 모든 필드 optional)
	 * @return 수정된 사업장 정보 (siteId: 사업장 ID, siteName: 사업장명, latitude: 위도, longitude: 경도, jibunAddress: 지번주소, roadAddress: 도로명주소, siteType: 사업장 유형)
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 * @throws ResourceNotFoundException 사업장을 찾을 수 없는 경우 (404)
	 * @throws ValidationException 사업장 데이터가 유효하지 않은 경우 (422)
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
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 * @throws ResourceNotFoundException 사업장을 찾을 수 없는 경우 (404)
	 */
	@DeleteMapping
	public ResponseEntity<Map<String, Object>> deleteSite(@RequestParam UUID siteId) {
		log.info("DELETE /api/sites?siteId={} - Deleting site", siteId);
		siteService.deleteSite(siteId);
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}
}
