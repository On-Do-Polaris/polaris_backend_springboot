package com.skax.physicalrisk.service;

import com.skax.physicalrisk.domain.site.entity.Site;
import com.skax.physicalrisk.domain.site.repository.SiteRepository;
import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.dto.request.site.CreateSiteRequest;
import com.skax.physicalrisk.dto.request.site.UpdateSiteRequest;
import com.skax.physicalrisk.dto.response.site.SiteResponse;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import com.skax.physicalrisk.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 사업장 서비스
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteService {

	private final SiteRepository siteRepository;
	private final UserRepository userRepository;

	/**
	 * 사업장 목록 조회
	 *
	 * @param pageable 페이징 정보
	 * @return 사업장 목록
	 */
	public Page<SiteResponse> getSites(Pageable pageable) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching sites for user: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		Page<Site> sites = siteRepository.findByUser(user, pageable);
		return sites.map(SiteResponse::from);
	}

	/**
	 * 사업장 검색
	 *
	 * @param keyword  검색 키워드
	 * @param pageable 페이징 정보
	 * @return 검색 결과
	 */
	public Page<SiteResponse> searchSites(String keyword, Pageable pageable) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Searching sites for user: {} with keyword: {}", userId, keyword);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		Page<Site> sites = siteRepository.searchByUserAndKeyword(user, keyword, pageable);
		return sites.map(SiteResponse::from);
	}

	/**
	 * 사업장 상세 조회
	 *
	 * @param siteId 사업장 ID
	 * @return 사업장 상세 정보
	 */
	public SiteResponse getSite(UUID siteId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching site: {} for user: {}", siteId, userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		Site site = siteRepository.findByIdAndUser(siteId, user)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND));

		return SiteResponse.from(site);
	}

	/**
	 * 사업장 생성
	 *
	 * @param request 생성 요청
	 * @return 생성된 사업장 정보
	 */
	@Transactional
	public SiteResponse createSite(CreateSiteRequest request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Creating site for user: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// 주소에서 도시 추출 (간단 구현)
		String city = extractCity(request.getAddress());

		Site site = Site.builder()
			.user(user)
			.name(request.getName())
			.address(request.getAddress())
			.city(city)
			.latitude(request.getLatitude())
			.longitude(request.getLongitude())
			.industry(request.getIndustry())
			.description(request.getDescription())
			.build();

		Site savedSite = siteRepository.save(site);
		log.info("Site created successfully: {}", savedSite.getId());

		return SiteResponse.from(savedSite);
	}

	/**
	 * 사업장 수정
	 *
	 * @param siteId  사업장 ID
	 * @param request 수정 요청
	 * @return 수정된 사업장 정보
	 */
	@Transactional
	public SiteResponse updateSite(UUID siteId, UpdateSiteRequest request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Updating site: {} for user: {}", siteId, userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		Site site = siteRepository.findByIdAndUser(siteId, user)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND));

		// 수정 가능한 필드만 업데이트
		if (request.getName() != null) {
			site.setName(request.getName());
		}
		if (request.getAddress() != null) {
			site.setAddress(request.getAddress());
			site.setCity(extractCity(request.getAddress()));
		}
		if (request.getIndustry() != null) {
			site.setIndustry(request.getIndustry());
		}
		if (request.getDescription() != null) {
			site.setDescription(request.getDescription());
		}

		Site savedSite = siteRepository.save(site);
		log.info("Site updated successfully: {}", siteId);

		return SiteResponse.from(savedSite);
	}

	/**
	 * 사업장 삭제
	 *
	 * @param siteId 사업장 ID
	 */
	@Transactional
	public void deleteSite(UUID siteId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Deleting site: {} for user: {}", siteId, userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		Site site = siteRepository.findByIdAndUser(siteId, user)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SITE_NOT_FOUND));

		siteRepository.delete(site);
		log.info("Site deleted successfully: {}", siteId);
	}

	/**
	 * 주소에서 도시명 추출 (간단 구현)
	 *
	 * @param address 주소
	 * @return 도시명
	 */
	private String extractCity(String address) {
		if (address == null || address.isEmpty()) {
			return null;
		}

		// 간단한 구현: 첫 번째 공백 앞의 텍스트를 도시로 간주
		String[] parts = address.split(" ");
		return parts.length > 0 ? parts[0] : null;
	}
}
