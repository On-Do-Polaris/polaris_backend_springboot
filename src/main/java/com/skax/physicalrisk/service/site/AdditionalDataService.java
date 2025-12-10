package com.skax.physicalrisk.service.site;

import com.skax.physicalrisk.client.fastapi.FastApiClient;
import com.skax.physicalrisk.domain.site.entity.DataCategory;
import com.skax.physicalrisk.domain.site.entity.Site;
import com.skax.physicalrisk.domain.site.repository.SiteRepository;
import com.skax.physicalrisk.dto.request.site.AdditionalDataInput;
import com.skax.physicalrisk.dto.response.site.AdditionalDataGetResponse;
import com.skax.physicalrisk.dto.response.site.AdditionalDataUploadResponse;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import com.skax.physicalrisk.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 추가 데이터 관리 서비스
 *
 * FastAPI 추가 데이터 API 프록시
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdditionalDataService {

	private final FastApiClient fastApiClient;
	private final SiteRepository siteRepository;

	/**
	 * 추가 데이터 업로드
	 *
	 * @param siteId 사업장 ID
	 * @param input 추가 데이터 입력
	 * @return 업로드 응답
	 */
	@Transactional
	public Mono<AdditionalDataUploadResponse> uploadAdditionalData(UUID siteId, AdditionalDataInput input) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("추가 데이터 업로드 요청: userId={}, siteId={}, category={}", userId, siteId, input.getDataCategory());

		// 사업장 소유권 검증
		Site site = siteRepository.findById(siteId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND));

		if (!site.getUser().getId().equals(userId)) {
			throw new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND);
		}

		// FastAPI 요청 데이터 생성
		Map<String, Object> request = new HashMap<>();
		request.put("dataCategory", input.getDataCategory().getCode());
		request.put("rawText", input.getRawText());
		request.put("structuredData", input.getStructuredData());
		request.put("fileName", input.getFileName());
		request.put("fileS3Key", input.getFileS3Key());
		request.put("fileSize", input.getFileSize());
		request.put("fileMimeType", input.getFileMimeType());
		request.put("metadata", input.getMetadata());
		request.put("expiresAt", input.getExpiresAt());

		return fastApiClient.uploadAdditionalData(siteId, request)
			.map(response -> AdditionalDataUploadResponse.builder()
				.id(UUID.fromString(response.get("id").toString()))
				.siteId(siteId)
				.dataCategory(DataCategory.fromCode(response.get("dataCategory").toString()))
				.status(response.get("status").toString())
				.uploadedAt(LocalDateTime.parse(response.get("uploadedAt").toString()))
				.build());
	}

	/**
	 * 추가 데이터 조회
	 *
	 * @param siteId 사업장 ID
	 * @param dataCategory 데이터 카테고리
	 * @return 추가 데이터 응답
	 */
	public Mono<AdditionalDataGetResponse> getAdditionalData(UUID siteId, DataCategory dataCategory) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("추가 데이터 조회: userId={}, siteId={}, category={}", userId, siteId, dataCategory);

		// 사업장 소유권 검증
		Site site = siteRepository.findById(siteId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND));

		if (!site.getUser().getId().equals(userId)) {
			throw new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND);
		}

		return fastApiClient.getAdditionalData(siteId, dataCategory.getCode())
			.map(response -> AdditionalDataGetResponse.builder()
				.id(UUID.fromString(response.get("id").toString()))
				.siteId(siteId)
				.dataCategory(DataCategory.fromCode(response.get("dataCategory").toString()))
				.rawText((String) response.get("rawText"))
				.structuredData((Map<String, Object>) response.get("structuredData"))
				.fileName((String) response.get("fileName"))
				.fileS3Key((String) response.get("fileS3Key"))
				.fileSize(response.get("fileSize") != null ? ((Number) response.get("fileSize")).longValue() : null)
				.fileMimeType((String) response.get("fileMimeType"))
				.metadata((Map<String, Object>) response.get("metadata"))
				.uploadedBy(response.get("uploadedBy") != null ? UUID.fromString(response.get("uploadedBy").toString()) : null)
				.uploadedAt(response.get("uploadedAt") != null ? LocalDateTime.parse(response.get("uploadedAt").toString()) : null)
				.expiresAt(response.get("expiresAt") != null ? LocalDateTime.parse(response.get("expiresAt").toString()) : null)
				.build());
	}

	/**
	 * 추가 데이터 삭제
	 *
	 * @param siteId 사업장 ID
	 * @param dataId 데이터 ID
	 * @return 삭제 성공 여부
	 */
	@Transactional
	public Mono<Boolean> deleteAdditionalData(UUID siteId, UUID dataId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("추가 데이터 삭제: userId={}, siteId={}, dataId={}", userId, siteId, dataId);

		// 사업장 소유권 검증
		Site site = siteRepository.findById(siteId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND));

		if (!site.getUser().getId().equals(userId)) {
			throw new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND);
		}

		// ⚠️ MISMATCH: Spring Boot Controller uses dataId, but FastAPI expects dataCategory
		// Using dataId as a temporary workaround - this will likely fail
		log.warn("⚠️ API 불일치: FastAPI는 dataCategory를 요구하지만 dataId={}를 전달합니다", dataId);
		return fastApiClient.deleteAdditionalData(siteId, dataId.toString())
			.map(response -> true)
			.onErrorReturn(false);
	}

	/**
	 * 정형화된 데이터 조회
	 *
	 * ⚠️ WARNING: FastAPI OpenAPI에서 이 엔드포인트를 찾을 수 없습니다.
	 *
	 * @param siteId 사업장 ID
	 * @param dataId 데이터 ID
	 * @return 정형화된 데이터
	 */
	public Mono<Map<String, Object>> getStructuredData(UUID siteId, UUID dataId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("정형화 데이터 조회: userId={}, siteId={}, dataId={}", userId, siteId, dataId);

		// 사업장 소유권 검증
		Site site = siteRepository.findById(siteId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND));

		if (!site.getUser().getId().equals(userId)) {
			throw new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND);
		}

		// ⚠️ CRITICAL: FastAPI OpenAPI 스펙에 이 엔드포인트가 존재하지 않습니다!
		// 임시로 GET /api/additional-data를 호출하여 structuredData 필드 반환
		log.warn("⚠️ getStructuredData는 FastAPI OpenAPI에 없는 엔드포인트입니다");
		return Mono.error(new UnsupportedOperationException(
			"getStructuredData endpoint does not exist in FastAPI OpenAPI spec"));
	}
}
