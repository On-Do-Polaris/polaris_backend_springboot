package com.skax.physicalrisk.service;

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

		// 위험도별 사업장 수
		long highRiskCount = siteRepository.countByUserAndRiskLevel(user, Site.RiskLevel.HIGH);
		long mediumRiskCount = siteRepository.countByUserAndRiskLevel(user, Site.RiskLevel.MODERATE);
		long lowRiskCount = siteRepository.countByUserAndRiskLevel(user, Site.RiskLevel.LOW);

		Map<String, Object> summary = new HashMap<>();
		summary.put("totalSites", highRiskCount + mediumRiskCount + lowRiskCount);
		summary.put("highRiskSites", highRiskCount);
		summary.put("mediumRiskSites", mediumRiskCount);
		summary.put("lowRiskSites", lowRiskCount);

		log.info("Dashboard summary fetched successfully for user: {}", userId);
		return summary;
	}
}
