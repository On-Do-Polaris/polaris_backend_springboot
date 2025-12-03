package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.auth.LoginRequest;
import com.skax.physicalrisk.dto.request.auth.PasswordResetConfirmRequest;
import com.skax.physicalrisk.dto.request.auth.PasswordResetRequest;
import com.skax.physicalrisk.dto.request.auth.RefreshTokenRequest;
import com.skax.physicalrisk.dto.request.auth.RegisterRequest;
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
	 * 회원가입
	 *
	 * @param request 회원가입 요청
	 * @return 생성된 사용자 ID (이메일)
	 */
	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
		log.info("POST /api/auth/register - Email: {}", request.getEmail());
		String userId = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("userId", userId));
	}

	/**
	 * 로그인
	 *
	 * @param request 로그인 요청
	 * @return 토큰 및 사용자 정보
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
	 */
	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout() {
		log.info("POST /api/auth/logout");
		String userId = SecurityUtil.getCurrentUserId().toString();
		authService.logout(userId);
		return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다"));
	}

	/**
	 * 토큰 갱신
	 *
	 * @param request 리프레시 토큰 요청
	 * @return 갱신된 액세스 토큰 및 리프레시 토큰
	 */
	@PostMapping("/refresh")
	public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		log.info("POST /api/auth/refresh");
		LoginResponse response = authService.refresh(request.getRefreshToken());
		return ResponseEntity.ok(response);
	}

	/**
	 * 비밀번호 재설정 요청
	 *
	 * @param request 비밀번호 재설정 요청
	 * @return 성공 메시지
	 */
	@PostMapping("/password/reset-request")
	public ResponseEntity<Map<String, String>> resetPasswordRequest(@Valid @RequestBody PasswordResetRequest request) {
		log.info("POST /api/auth/password/reset-request - Email: {}", request.getEmail());
		authService.requestPasswordReset(request);
		return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 이메일이 발송되었습니다"));
	}

	/**
	 * 비밀번호 재설정 확인
	 *
	 * @param request 비밀번호 재설정 확인 요청
	 * @return 성공 메시지
	 */
	@PostMapping("/password/reset-confirm")
	public ResponseEntity<Map<String, String>> resetPasswordConfirm(@Valid @RequestBody PasswordResetConfirmRequest request) {
		log.info("POST /api/auth/password/reset-confirm");
		authService.confirmPasswordReset(request);
		return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다"));
	}
}
