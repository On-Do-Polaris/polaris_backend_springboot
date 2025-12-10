package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.auth.*;
import com.skax.physicalrisk.dto.response.auth.LoginResponse;
import com.skax.physicalrisk.security.SecurityUtil;
import com.skax.physicalrisk.service.user.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 인증 컨트롤러
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * 회원가입 이메일 인증 요청 (1단계)
	 *
	 * @param request 이메일 요청 (email: 이메일 주소)
	 * @return 빈 응답 (성공)
	 * @throws DuplicateResourceException 이메일이 이미 존재하는 경우 (409)
	 * @throws BusinessException 이메일 발송에 실패한 경우 (503)
	 */
	@PostMapping("/register-email")
	public ResponseEntity<Map<String, Object>> registerEmail(@Valid @RequestBody RegisterEmailRequest request) {
		log.info("POST /api/auth/register-email - Email: {}", request.getEmail());
		authService.sendRegisterEmail(request.getEmail());
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}

	/**
	 * 회원가입 인증번호 확인 (2단계)
	 *
	 * @param request 인증번호 확인 요청 (email: 이메일 주소, verificationCode: 6자리 인증번호)
	 * @return 빈 응답 (성공)
	 * @throws ValidationException 인증번호가 일치하지 않거나 만료된 경우 (422)
	 */
	@PostMapping("/register-verificationCode")
	public ResponseEntity<Map<String, Object>> registerVerificationCode(@Valid @RequestBody VerifyCodeRequest request) {
		log.info("POST /api/auth/register-verificationCode - Email: {}", request.getEmail());
		authService.verifyRegisterCode(request.getEmail(), request.getVerificationCode());
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}

	/**
	 * 회원가입 (3단계)
	 *
	 * @param request 회원가입 요청 (email: 이메일 주소, name: 사용자 이름, password: 비밀번호)
	 * @return 빈 응답 (성공)
	 * @throws DuplicateResourceException 이메일이 이미 존재하는 경우 (409)
	 * @throws ValidationException 이메일 인증이 완료되지 않은 경우 (422)
	 */
	@PostMapping("/register")
	public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
		log.info("POST /api/auth/register - Email: {}", request.getEmail());
		authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Collections.emptyMap());
	}

	/**
	 * 로그인
	 *
	 * @param request 로그인 요청 (email: 이메일 주소, password: 비밀번호)
	 * @return 토큰 및 사용자 정보 (accessToken: 액세스 토큰, refreshToken: 리프레시 토큰, userId: 사용자 ID)
	 * @throws UnauthorizedException 이메일이 존재하지 않거나 비밀번호가 일치하지 않는 경우 (401)
	 */
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		log.info("POST /api/auth/login - Email: {}", request.getEmail());
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 로그아웃
	 *
	 * @return 성공 메시지
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 */
	@PostMapping("/logout")
	public ResponseEntity<Map<String, Object>> logout() {
		log.info("POST /api/auth/logout");
		String userId = SecurityUtil.getCurrentUserId().toString();
		authService.logout(userId);
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}

	/**
	 * 토큰 갱신
	 *
	 * @param request 리프레시 토큰 요청 (refreshToken: 리프레시 토큰)
	 * @return 갱신된 액세스 토큰 및 리프레시 토큰 (accessToken: 새 액세스 토큰, refreshToken: 새 리프레시 토큰, userId: 사용자 ID)
	 * @throws UnauthorizedException 리프레시 토큰이 유효하지 않거나 만료된 경우 (401)
	 */
	@PostMapping("/refresh")
	public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		log.info("POST /api/auth/refresh");
		LoginResponse response = authService.refresh(request.getRefreshToken());
		return ResponseEntity.ok(response);
	}

	/**
	 * 비밀번호 재설정 이메일 인증 요청
	 *
	 * @param request 비밀번호 재설정 이메일 요청 (email: 이메일 주소)
	 * @return 성공 메시지
	 * @throws ResourceNotFoundException 이메일이 존재하지 않는 경우 (404)
	 * @throws BusinessException 이메일 발송에 실패한 경우 (503)
	 */
	@PostMapping("/password/reset-email")
	public ResponseEntity<Map<String, Object>> resetPasswordEmail(@Valid @RequestBody PasswordResetEmailRequest request) {
		log.info("POST /api/auth/password/reset-email - Email: {}", request.getEmail());
		authService.sendPasswordResetEmail(request.getEmail());
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}

	/**
	 * 비밀번호 재설정 요청 (기존 토큰 방식 - 호환성 유지)
	 *
	 * @param request 비밀번호 재설정 요청 (email: 이메일 주소)
	 * @return 성공 메시지
	 * @throws ResourceNotFoundException 이메일이 존재하지 않는 경우 (404)
	 * @throws BusinessException 이메일 발송에 실패한 경우 (503)
	 */
	@PostMapping("/password/reset-request")
	public ResponseEntity<Map<String, Object>> resetPasswordRequest(@Valid @RequestBody PasswordResetRequest request) {
		log.info("POST /api/auth/password/reset-request - Email: {}", request.getEmail());
		authService.requestPasswordReset(request);
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}

	/**
	 * 비밀번호 재설정 확인
	 *
	 * @param request 비밀번호 재설정 확인 요청 (email: 이메일 주소, verificationCode: 인증번호, newPassword: 새 비밀번호)
	 * @return 성공 메시지
	 * @throws ValidationException 인증번호가 일치하지 않거나 만료된 경우 (422)
	 * @throws ResourceNotFoundException 이메일이 존재하지 않는 경우 (404)
	 */
	@PostMapping("/password/reset-confirm")
	public ResponseEntity<Map<String, Object>> resetPasswordConfirm(@Valid @RequestBody PasswordResetConfirmRequest request) {
		log.info("POST /api/auth/password/reset-confirm");
		authService.confirmPasswordReset(request);
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}
}
