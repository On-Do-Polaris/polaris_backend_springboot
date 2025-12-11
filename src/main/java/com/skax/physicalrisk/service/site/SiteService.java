package com.skax.physicalrisk.service.site;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 사업장 서비스
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
	 * 사용자의 전체 사업장 목록 조회
	 *
	 * @return 사업장 목록
	 */
	public SiteResponse getSites() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching sites for user: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		List<Site> sites = siteRepository.findByUser(user);

		List<SiteResponse.SiteInfo> siteInfos = sites.stream()
			.map(site -> SiteResponse.SiteInfo.builder()
				.siteId(site.getId())
				.siteName(site.getName())
				.latitude(site.getLatitude())
				.longitude(site.getLongitude())
				.jibunAddress(site.getJibunAddress())
				.roadAddress(site.getRoadAddress())
				.siteType(site.getType())
				.build())
			.collect(Collectors.toList());

		return SiteResponse.builder()
			.sites(siteInfos)
			.build();
	}

	/**
	 * 사업장 생성
	 *
	 * @param request 생성 요청
	 * @return 생성된 사업장 정보
	 */
	@Transactional
	public SiteResponse.SiteInfo createSite(CreateSiteRequest request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Creating site for user: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// 동일한 위경도를 가진 사업장이 이미 존재하는지 확인
		if (request.getLatitude() != null && request.getLongitude() != null) {
			siteRepository.findByLatitudeAndLongitudeAndUser(
				request.getLatitude(),
				request.getLongitude(),
				user
			).ifPresent(existingSite -> {
				log.warn("Duplicate site coordinates detected: lat={}, lon={} for user={}",
					request.getLatitude(), request.getLongitude(), userId);
				throw new com.skax.physicalrisk.exception.BusinessException(
					ErrorCode.DUPLICATE_SITE_COORDINATES,
					"동일한 위경도를 가진 사업장이 이미 존재합니다"
				);
			});
		}

		Site site = Site.builder()
			.user(user)
			.name(request.getName())
			.roadAddress(request.getRoadAddress())
			.jibunAddress(request.getJibunAddress())
			.latitude(request.getLatitude())
			.longitude(request.getLongitude())
			.type(request.getType())
			.build();

		Site savedSite = siteRepository.save(site);
		log.info("Site created successfully: {}", savedSite.getId());

		return SiteResponse.SiteInfo.builder()
			.siteId(savedSite.getId())
			.siteName(savedSite.getName())
			.latitude(savedSite.getLatitude())
			.longitude(savedSite.getLongitude())
			.jibunAddress(savedSite.getJibunAddress())
			.roadAddress(savedSite.getRoadAddress())
			.siteType(savedSite.getType())
			.build();
	}

	/**
	 * 사업장 수정
	 *
	 * @param siteId  사업장 ID
	 * @param request 수정 요청
	 * @return 수정된 사업장 정보
	 */
	@Transactional
	public SiteResponse.SiteInfo updateSite(UUID siteId, UpdateSiteRequest request) {
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
		if (request.getRoadAddress() != null) {
			site.setRoadAddress(request.getRoadAddress());
		}
		if (request.getJibunAddress() != null) {
			site.setJibunAddress(request.getJibunAddress());
		}
		if (request.getLatitude() != null) {
			site.setLatitude(request.getLatitude());
		}
		if (request.getLongitude() != null) {
			site.setLongitude(request.getLongitude());
		}
		if (request.getType() != null) {
			site.setType(request.getType());
		}

		Site savedSite = siteRepository.save(site);
		log.info("Site updated successfully: {}", siteId);

		return SiteResponse.SiteInfo.builder()
			.siteId(savedSite.getId())
			.siteName(savedSite.getName())
			.latitude(savedSite.getLatitude())
			.longitude(savedSite.getLongitude())
			.jibunAddress(savedSite.getJibunAddress())
			.roadAddress(savedSite.getRoadAddress())
			.siteType(savedSite.getType())
			.build();
	}

	/**
	 * 사업장 이름으로 검색 (v0.2 신규)
	 *
	 * @param siteName 사업장명
	 * @return 사업장 정보
	 */
	public SiteResponse.SiteInfo getSiteByName(String siteName) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Searching site by name: {} for user: {}", siteName, userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// 사용자의 사업장 중 이름으로 검색
		Site site = siteRepository.findByNameAndUser(siteName, user)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SITE_NAME_NOT_FOUND));

		return SiteResponse.SiteInfo.builder()
			.siteId(site.getId())
			.siteName(site.getName())
			.latitude(site.getLatitude())
			.longitude(site.getLongitude())
			.jibunAddress(site.getJibunAddress())
			.roadAddress(site.getRoadAddress())
			.siteType(site.getType())
			.build();
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
}
