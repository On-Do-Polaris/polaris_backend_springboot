package com.skax.physicalrisk.security;

import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

/**
 * Security 유틸리티 클래스
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
public class SecurityUtil {

	private SecurityUtil() {
		// Utility class
	}

	/**
	 * 현재 로그인한 사용자 ID 조회
	 *
	 * @return 사용자 ID
	 * @throws UnauthorizedException 인증되지 않은 경우
	 */
	public static UUID getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			log.error("User is not authenticated");
			throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof UserDetails) {
			String username = ((UserDetails) principal).getUsername();
			try {
				return UUID.fromString(username);
			} catch (IllegalArgumentException e) {
				log.error("Invalid UUID format: {}", username);
				throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
			}
		}

		log.error("Principal is not an instance of UserDetails");
		throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
	}
}
