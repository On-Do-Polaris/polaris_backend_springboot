package com.skax.physicalrisk.service.user;

import com.skax.physicalrisk.domain.user.entity.PasswordResetToken;
import com.skax.physicalrisk.domain.user.entity.RefreshToken;
import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.entity.VerificationCode;
import com.skax.physicalrisk.domain.user.repository.PasswordResetTokenRepository;
import com.skax.physicalrisk.domain.user.repository.RefreshTokenRepository;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.dto.request.auth.LoginRequest;
import com.skax.physicalrisk.dto.request.auth.PasswordResetConfirmRequest;
import com.skax.physicalrisk.dto.request.auth.PasswordResetRequest;
import com.skax.physicalrisk.dto.request.auth.RegisterRequest;
import com.skax.physicalrisk.dto.response.auth.LoginResponse;
import com.skax.physicalrisk.exception.BusinessException;
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
import java.util.Optional;
import java.util.UUID;

/**
 * 인증 서비스
 *
 * @author SKAX Team
 */
@Slf4j
@Service
public class AuthService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final Optional<EmailService> emailService;
	private final VerificationService verificationService;

	public AuthService(UserRepository userRepository,
					   RefreshTokenRepository refreshTokenRepository,
					   PasswordResetTokenRepository passwordResetTokenRepository,
					   PasswordEncoder passwordEncoder,
					   JwtTokenProvider jwtTokenProvider,
					   Optional<EmailService> emailService,
					   VerificationService verificationService) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.emailService = emailService;
		this.verificationService = verificationService;
	}

	/**
	 * 회원가입 이메일 인증 요청 (1단계)
	 *
	 * @param email 이메일
	 */
	@Transactional
	public void sendRegisterEmail(String email) {
		log.info("Sending registration verification email to: {}", email);

		// 이메일 중복 확인
		if (userRepository.existsByEmail(email)) {
			log.error("Email already exists: {}", email);
			throw new DuplicateResourceException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}

		// 인증번호 발송
		verificationService.sendVerificationEmail(email, VerificationCode.Purpose.REGISTER);
	}

	/**
	 * 회원가입 인증번호 확인 (2단계)
	 *
	 * @param email 이메일
	 * @param code  인증번호
	 */
	@Transactional
	public void verifyRegisterCode(String email, String code) {
		log.info("Verifying registration code for email: {}", email);

		// 인증번호 검증
		verificationService.verifyCode(email, code, VerificationCode.Purpose.REGISTER);
	}

	/**
	 * 회원가입 (3단계)
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

		// 이메일 인증 완료 여부 확인
		if (!verificationService.isEmailVerified(request.getEmail(), VerificationCode.Purpose.REGISTER)) {
			log.error("Email not verified: {}", request.getEmail());
			throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED, "이메일 인증이 완료되지 않았습니다.");
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

		// 인증 코드 삭제
		verificationService.clearVerifiedCode(request.getEmail(), VerificationCode.Purpose.REGISTER);

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

		// 사용자 조회 - EMAIL_NOT_FOUND
		User user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> {
				log.error("Email not found: {}", request.getEmail());
				return new UnauthorizedException(ErrorCode.EMAIL_NOT_FOUND);
			});

		// 비밀번호 확인 - PASSWORD_MISMATCH
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			log.error("Password mismatch for user: {}", request.getEmail());
			throw new UnauthorizedException(ErrorCode.PASSWORD_MISMATCH);
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
	 * 비밀번호 재설정 이메일 인증 요청 (1단계)
	 *
	 * @param email 이메일
	 */
	@Transactional
	public void sendPasswordResetEmail(String email) {
		log.info("Sending password reset verification email to: {}", email);

		// 사용자 조회
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> {
				log.error("Email not found: {}", email);
				return new ResourceNotFoundException(ErrorCode.EMAIL_NOT_FOUND);
			});

		// 인증번호 발송
		verificationService.sendVerificationEmail(email, VerificationCode.Purpose.PASSWORD_RESET);
	}

	/**
	 * 비밀번호 재설정 인증번호 확인 (2단계)
	 *
	 * @param email 이메일
	 * @param code  인증번호
	 */
	@Transactional
	public void verifyPasswordResetCode(String email, String code) {
		log.info("Verifying password reset code for email: {}", email);

		// 사용자 존재 여부 확인
		userRepository.findByEmail(email)
			.orElseThrow(() -> {
				log.error("Email not found: {}", email);
				return new ResourceNotFoundException(ErrorCode.EMAIL_NOT_FOUND);
			});

		// 인증번호 검증
		verificationService.verifyCode(email, code, VerificationCode.Purpose.PASSWORD_RESET);
	}

	/**
	 * 비밀번호 재설정 완료 (3단계)
	 *
	 * @param email       이메일
	 * @param newPassword 새 비밀번호
	 */
	@Transactional
	public void completePasswordReset(String email, String newPassword) {
		log.info("Completing password reset for email: {}", email);

		// 사용자 조회
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> {
				log.error("Email not found: {}", email);
				return new ResourceNotFoundException(ErrorCode.EMAIL_NOT_FOUND);
			});

		// 인증 완료 여부 확인
		if (!verificationService.isEmailVerified(email, VerificationCode.Purpose.PASSWORD_RESET)) {
			log.error("Email not verified for password reset: {}", email);
			throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED, "이메일 인증이 완료되지 않았습니다.");
		}

		// 비밀번호 변경
		user.updatePassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		// 인증 코드 삭제
		verificationService.clearVerifiedCode(email, VerificationCode.Purpose.PASSWORD_RESET);

		log.info("Password reset successful for user: {}", user.getId());
	}
}
