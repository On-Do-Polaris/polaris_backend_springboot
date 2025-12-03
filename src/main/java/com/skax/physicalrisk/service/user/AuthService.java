package com.skax.physicalrisk.service.user;

import com.skax.physicalrisk.domain.user.entity.PasswordResetToken;
import com.skax.physicalrisk.domain.user.entity.RefreshToken;
import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.PasswordResetTokenRepository;
import com.skax.physicalrisk.domain.user.repository.RefreshTokenRepository;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.dto.request.auth.LoginRequest;
import com.skax.physicalrisk.dto.request.auth.PasswordResetConfirmRequest;
import com.skax.physicalrisk.dto.request.auth.PasswordResetRequest;
import com.skax.physicalrisk.dto.request.auth.RegisterRequest;
import com.skax.physicalrisk.dto.response.auth.LoginResponse;
import com.skax.physicalrisk.exception.DuplicateResourceException;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import com.skax.physicalrisk.exception.UnauthorizedException;
import com.skax.physicalrisk.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final EmailService emailService;

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

		// 사용자 생성
		User user = User.builder()
			.email(request.getEmail())
			.name(request.getName())
			.password(passwordEncoder.encode(request.getPassword()))
			.language("ko")
			.build();

		User savedUser = userRepository.save(user);
		log.info("User registered successfully: {}", savedUser.getId());

		return savedUser.getEmail();
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

		// 토큰 생성
		String accessToken = jwtTokenProvider.createAccessToken(user.getId());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

		// Refresh Token을 DB에 저장
		RefreshToken refreshTokenEntity = RefreshToken.builder()
			.user(user)
			.token(refreshToken)
			.expiresAt(LocalDateTime.now().plusDays(7))
			.build();
		refreshTokenRepository.save(refreshTokenEntity);

		log.info("User logged in successfully: {}", user.getId());

		return LoginResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.userId(user.getEmail())
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

		// DB에서 사용자의 모든 Refresh Token 폐기
		User user = userRepository.findById(UUID.fromString(userId))
			.orElseThrow(() -> new UnauthorizedException(ErrorCode.USER_NOT_FOUND));

		refreshTokenRepository.revokeAllByUser(user);

		log.info("User logged out successfully: {}", userId);
	}

	/**
	 * 토큰 갱신
	 *
	 * @param refreshToken 리프레시 토큰
	 * @return 새로운 액세스 토큰 및 리프레시 토큰
	 */
	@Transactional
	public LoginResponse refresh(String refreshToken) {
		log.info("Refreshing token");

		// Refresh Token 검증
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			log.error("Invalid refresh token");
			throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
		}

		// DB에서 Refresh Token 조회
		RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
			.orElseThrow(() -> {
				log.error("Refresh token not found in database");
				return new UnauthorizedException(ErrorCode.INVALID_TOKEN);
			});

		// 토큰 유효성 확인
		if (!tokenEntity.isValid()) {
			log.error("Refresh token is expired or revoked");
			throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
		}

		User user = tokenEntity.getUser();

		// 기존 Refresh Token 폐기
		tokenEntity.revoke();

		// 새로운 토큰 생성
		String newAccessToken = jwtTokenProvider.createAccessToken(user.getId());
		String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

		// 새로운 Refresh Token을 DB에 저장
		RefreshToken newTokenEntity = RefreshToken.builder()
			.user(user)
			.token(newRefreshToken)
			.expiresAt(LocalDateTime.now().plusDays(7))
			.build();
		refreshTokenRepository.save(newTokenEntity);

		log.info("Token refreshed successfully for user: {}", user.getId());

		return LoginResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.userId(user.getEmail())
			.build();
	}

	/**
	 * 비밀번호 재설정 요청
	 *
	 * @param request 비밀번호 재설정 요청
	 */
	@Transactional
	public void requestPasswordReset(PasswordResetRequest request) {
		log.info("Password reset requested for email: {}", request.getEmail());

		// 사용자 조회
		User user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> {
				log.error("User not found: {}", request.getEmail());
				return new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND);
			});

		// 재설정 토큰 생성
		String token = UUID.randomUUID().toString();

		// 토큰 저장 (30분 유효)
		PasswordResetToken resetToken = PasswordResetToken.builder()
			.token(token)
			.user(user)
			.expiresAt(LocalDateTime.now().plusMinutes(30))
			.build();
		passwordResetTokenRepository.save(resetToken);

		// 이메일 발송
		emailService.sendPasswordResetEmail(user.getEmail(), token);

		log.info("Password reset email sent to: {}", user.getEmail());
	}

	/**
	 * 비밀번호 재설정 확인
	 *
	 * @param request 비밀번호 재설정 확인 요청
	 */
	@Transactional
	public void confirmPasswordReset(PasswordResetConfirmRequest request) {
		log.info("Confirming password reset with token");

		// 토큰 조회
		PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
			.orElseThrow(() -> {
				log.error("Invalid reset token");
				return new UnauthorizedException(ErrorCode.INVALID_TOKEN);
			});

		// 토큰 유효성 검증
		if (!resetToken.isValid()) {
			log.error("Reset token is expired or already used");
			throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
		}

		// 비밀번호 변경
		User user = resetToken.getUser();
		user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);

		// 토큰 사용 처리
		resetToken.markAsUsed();

		log.info("Password reset successful for user: {}", user.getId());
	}
}
