package com.skax.physicalrisk.service.user;

import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.dto.request.auth.LoginRequest;
import com.skax.physicalrisk.dto.request.auth.RegisterRequest;
import com.skax.physicalrisk.dto.response.auth.LoginResponse;
import com.skax.physicalrisk.exception.DuplicateResourceException;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.UnauthorizedException;
import com.skax.physicalrisk.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인증 서비스
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	// Redis 대신 인메모리 저장소 사용 (로컬 개발용)
	private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();

	/**
	 * 회원가입
	 *
	 * @param request 회원가입 요청
	 * @return 사용자 ID (이메일)
	 */
	@Transactional
	public String register(RegisterRequest request) {
		log.info("Registering new user with email: {}", request.getEmail());

		// 이메일 중복 확인
		if (userRepository.existsByEmail(request.getEmail())) {
			log.error("Email already exists: {}", request.getEmail());
			throw new DuplicateResourceException(ErrorCode.DUPLICATE_EMAIL);
		}

		// 사용자 생성 (organization 필드 제거됨)
		User user = User.builder()
			.email(request.getEmail())
			.name(request.getName())
			.password(passwordEncoder.encode(request.getPassword()))
			.language("ko")  // 기본 언어 설정
			.role(User.UserRole.USER)
			.build();

		User savedUser = userRepository.save(user);
		log.info("User registered successfully: {}", savedUser.getId());

		return savedUser.getEmail();  // 이메일 반환
	}

	/**
	 * 로그인
	 *
	 * @param request 로그인 요청
	 * @return 로그인 응답 (토큰 및 사용자 ID)
	 */
	@Transactional
	public LoginResponse login(LoginRequest request) {
		log.info("Login attempt for email: {}", request.getEmail());

		// 사용자 조회
		User user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> {
				log.error("User not found: {}", request.getEmail());
				return new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS);
			});

		// 비밀번호 확인
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			log.error("Invalid password for user: {}", request.getEmail());
			throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS);
		}

		// 마지막 로그인 시간 업데이트
		user.updateLastLogin();
		userRepository.save(user);

		// 토큰 생성
		String accessToken = jwtTokenProvider.createAccessToken(user.getId());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

		// Refresh Token을 메모리에 저장 (로컬 개발용)
		String tokenKey = "refresh_token:" + user.getId();
		refreshTokenStore.put(tokenKey, refreshToken);

		log.info("User logged in successfully: {}", user.getId());

		return LoginResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.userId(user.getEmail())  // 이메일을 userId로 반환
			.build();
	}

	/**
	 * 로그아웃
	 *
	 * @param userId 사용자 ID
	 */
	@Transactional
	public void logout(String userId) {
		log.info("Logging out user: {}", userId);

		// 메모리에서 Refresh Token 삭제
		String tokenKey = "refresh_token:" + userId;
		refreshTokenStore.remove(tokenKey);

		log.info("User logged out successfully: {}", userId);
	}
}
