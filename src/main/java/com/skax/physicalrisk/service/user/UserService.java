package com.skax.physicalrisk.service.user;

import com.skax.physicalrisk.domain.analysis.repository.AnalysisJobRepository;
import com.skax.physicalrisk.domain.report.repository.ReportRepository;
import com.skax.physicalrisk.domain.site.entity.Site;
import com.skax.physicalrisk.domain.site.repository.SiteRepository;
import com.skax.physicalrisk.domain.user.entity.PasswordResetToken;
import com.skax.physicalrisk.domain.user.entity.RefreshToken;
import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.PasswordResetTokenRepository;
import com.skax.physicalrisk.domain.user.repository.RefreshTokenRepository;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.dto.request.user.UpdateUserRequest;
import com.skax.physicalrisk.dto.response.user.UserResponse;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import com.skax.physicalrisk.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 사용자 서비스
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final SiteRepository siteRepository;
	private final ReportRepository reportRepository;
	private final AnalysisJobRepository analysisJobRepository;

	/**
	 * 현재 사용자 정보 조회
	 *
	 * @return 사용자 정보
	 */
	public UserResponse getCurrentUser() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching current user: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		return UserResponse.builder()
			.email(user.getEmail())
			.name(user.getName())
			.language(user.getLanguage())
			.build();
	}

	/**
	 * 사용자 정보 수정
	 *
	 * @param request 수정 요청
	 * @return 수정된 사용자 정보
	 */
	@Transactional
	public UserResponse updateUser(UpdateUserRequest request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Updating user: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// 언어 설정만 수정 가능
		if (request.getLanguage() != null) {
			user.setLanguage(request.getLanguage());
		}

		User savedUser = userRepository.save(user);
		log.info("User updated successfully: {}", userId);

		return UserResponse.builder()
			.email(savedUser.getEmail())
			.name(savedUser.getName())
			.language(savedUser.getLanguage())
			.build();
	}

	/**
	 * 사용자 삭제 (탈퇴)
	 *
	 * 사용자 삭제 시 연관된 데이터를 계층적으로 삭제하여 외래키 제약 조건 위반을 방지
	 * 삭제 순서:
	 * 1. AnalysisJob 삭제 (Site의 자식)
	 * 2. Report 삭제 (Site의 자식)
	 * 3. Site 삭제 (User의 자식)
	 * 4. RefreshToken 삭제 (User의 자식)
	 * 5. PasswordResetToken 삭제 (User의 자식)
	 * 6. User 삭제
	 */
	@Transactional
	public void deleteUser() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Deleting user and all associated data: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// 1. 사용자의 Report 삭제 (User와 직접 연결)
		reportRepository.findByUser(user).ifPresent(report -> {
			reportRepository.delete(report);
			log.info("Deleted report for user: {}", userId);
		});

		// 2. 사용자의 모든 사업장 조회
		List<Site> sites = siteRepository.findByUser(user);

		if (!sites.isEmpty()) {
			log.info("Found {} sites for user: {}", sites.size(), userId);

			for (Site site : sites) {
				// 각 사업장의 AnalysisJob 삭제
				int deletedJobs = analysisJobRepository.findAll().stream()
					.filter(job -> job.getSite().getId().equals(site.getId()))
					.mapToInt(job -> {
						analysisJobRepository.delete(job);
						return 1;
					})
					.sum();
				if (deletedJobs > 0) {
					log.info("Deleted {} analysis jobs for site: {}", deletedJobs, site.getId());
				}
			}

			// 3. 모든 Site 삭제
			siteRepository.deleteAll(sites);
			log.info("Deleted {} sites for user: {}", sites.size(), userId);
		}

		// 4. RefreshToken 삭제
		List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(user);
		if (!refreshTokens.isEmpty()) {
			refreshTokenRepository.deleteAll(refreshTokens);
			log.info("Deleted {} refresh tokens for user: {}", refreshTokens.size(), userId);
		}

		// 5. PasswordResetToken 삭제
		List<PasswordResetToken> passwordResetTokens = passwordResetTokenRepository.findAll().stream()
			.filter(token -> token.getUser().getId().equals(userId))
			.toList();
		if (!passwordResetTokens.isEmpty()) {
			passwordResetTokenRepository.deleteAll(passwordResetTokens);
			log.info("Deleted {} password reset tokens for user: {}", passwordResetTokens.size(), userId);
		}

		// 6. User 삭제
		userRepository.delete(user);

		log.info("User and all associated data deleted successfully: {}", userId);
	}
}
