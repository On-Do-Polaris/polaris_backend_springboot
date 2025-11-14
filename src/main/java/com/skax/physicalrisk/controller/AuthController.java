package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.auth.LoginRequest;
import com.skax.physicalrisk.dto.request.auth.RegisterRequest;
import com.skax.physicalrisk.dto.response.auth.LoginResponse;
import com.skax.physicalrisk.dto.response.user.UserResponse;
import com.skax.physicalrisk.security.SecurityUtil;
import com.skax.physicalrisk.service.AuthService;
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
	 * @return 생성된 사용자 정보
	 */
	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		log.info("POST /api/auth/register - Email: {}", request.getEmail());
		UserResponse response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
	 * 토큰 갱신 (미구현)
	 *
	 * @return 갱신된 토큰
	 */
	@PostMapping("/refresh")
	public ResponseEntity<Map<String, String>> refresh() {
		log.info("POST /api/auth/refresh - Not implemented yet");
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
			.body(Map.of("message", "Not implemented yet"));
	}

	/**
	 * 비밀번호 재설정 요청 (미구현)
	 *
	 * @return 성공 메시지
	 */
	@PostMapping("/password/reset-request")
	public ResponseEntity<Map<String, String>> resetPasswordRequest() {
		log.info("POST /api/auth/password/reset-request - Not implemented yet");
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
			.body(Map.of("message", "Not implemented yet"));
	}

	/**
	 * 비밀번호 재설정 확인 (미구현)
	 *
	 * @return 성공 메시지
	 */
	@PostMapping("/password/reset-confirm")
	public ResponseEntity<Map<String, String>> resetPasswordConfirm() {
		log.info("POST /api/auth/password/reset-confirm - Not implemented yet");
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
			.body(Map.of("message", "Not implemented yet"));
	}
}
