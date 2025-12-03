package com.skax.physicalrisk.service.site;

import com.skax.physicalrisk.domain.site.entity.Site;
import com.skax.physicalrisk.domain.site.repository.SiteRepository;
import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import com.skax.physicalrisk.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 대시보드 서비스
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
public class DashboardService {

	private final SiteRepository siteRepository;
	private final UserRepository userRepository;

	/**
	 * 대시보드 요약 정보 조회
	 *
	 * @return 대시보드 요약 정보
	 */
	public Map<String, Object> getDashboardSummary() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching dashboard summary for user: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// 전체 사업장 수
		long totalSites = siteRepository.countByUser(user);

		Map<String, Object> summary = new HashMap<>();
		summary.put("totalSites", totalSites);
		summary.put("highRiskSites", 0L);
		summary.put("mediumRiskSites", 0L);
		summary.put("lowRiskSites", 0L);

		log.info("Dashboard summary fetched successfully for user: {}", userId);
		return summary;
	}
}
