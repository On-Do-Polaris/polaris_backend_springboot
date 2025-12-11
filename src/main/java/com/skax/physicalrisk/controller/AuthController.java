package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.auth.*;
import com.skax.physicalrisk.dto.response.ErrorResponse;
import com.skax.physicalrisk.dto.response.auth.LoginResponse;
import com.skax.physicalrisk.security.SecurityUtil;
import com.skax.physicalrisk.service.user.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
	@Operation(
		summary = "회원가입 이메일 인증 요청",
		description = "회원가입을 위한 6자리 인증번호를 이메일로 발송합니다. 이메일이 이미 존재하는 경우 409 Conflict 에러를 반환합니다."
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "이메일 주소",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = RegisterEmailRequest.class),
			examples = @ExampleObject(
				value = "{\"email\": \"user@example.com\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "200",
		description = "이메일 발송 성공",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(value = "{\"result\": \"success\", \"message\": \"인증번호가 이메일로 발송되었습니다.\"}")
		)
	)
	@ApiResponse(
		responseCode = "409",
		description = "이메일이 이미 존재함",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"이미 존재하는 이메일입니다.\", \"errorCode\": \"EMAIL_ALREADY_EXISTS\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@ApiResponse(
		responseCode = "503",
		description = "이메일 발송 실패",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"이메일 발송에 실패했습니다.\", \"errorCode\": \"EMAIL_SEND_FAILED\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@PostMapping("/register-email")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<Void>> registerEmail(@Valid @RequestBody RegisterEmailRequest request) {
		log.info("POST /api/auth/register-email - Email: {}", request.getEmail());
		authService.sendRegisterEmail(request.getEmail());
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success("인증번호가 이메일로 발송되었습니다."));
	}

	/**
	 * 회원가입 인증번호 확인 (2단계)
	 *
	 * @param request 인증번호 확인 요청 (email: 이메일 주소, verificationCode: 6자리 인증번호)
	 * @return 빈 응답 (성공)
	 * @throws ValidationException 인증번호가 일치하지 않거나 만료된 경우 (422)
	 */
	@Operation(
		summary = "회원가입 인증번호 확인",
		description = "이메일로 발송된 6자리 인증번호를 확인합니다. 인증번호가 일치하지 않거나 만료된 경우 422 에러를 반환합니다."
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "이메일 주소와 인증번호",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = VerifyCodeRequest.class),
			examples = @ExampleObject(
				value = "{\"email\": \"user@example.com\", \"verificationCode\": \"123456\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "200",
		description = "인증 성공",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(value = "{\"result\": \"success\", \"message\": \"인증이 완료되었습니다.\"}")
		)
	)
	@ApiResponse(
		responseCode = "422",
		description = "인증번호가 일치하지 않거나 만료됨",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"인증번호가 일치하지 않거나 만료되었습니다.\", \"errorCode\": \"INVALID_VERIFICATION_CODE\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@PostMapping("/register-verificationCode")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<Void>> registerVerificationCode(@Valid @RequestBody VerifyCodeRequest request) {
		log.info("POST /api/auth/register-verificationCode - Email: {}", request.getEmail());
		authService.verifyRegisterCode(request.getEmail(), request.getVerificationCode());
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success("인증이 완료되었습니다."));
	}

	/**
	 * 회원가입 (3단계)
	 *
	 * @param request 회원가입 요청 (email: 이메일 주소, name: 사용자 이름, password: 비밀번호)
	 * @return 빈 응답 (성공)
	 * @throws DuplicateResourceException 이메일이 이미 존재하는 경우 (409)
	 * @throws ValidationException 이메일 인증이 완료되지 않은 경우 (422)
	 */
	@Operation(
		summary = "회원가입 완료",
		description = "이메일 인증 완료 후 사용자 정보를 등록하여 회원가입을 완료합니다."
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "회원가입 정보 (이메일, 이름, 비밀번호)",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = RegisterRequest.class),
			examples = @ExampleObject(
				value = "{\"email\": \"user@example.com\", \"name\": \"홍길동\", \"password\": \"password123\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "201",
		description = "회원가입 성공",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(value = "{\"result\": \"success\", \"message\": \"회원가입이 완료되었습니다.\"}")
		)
	)
	@ApiResponse(
		responseCode = "409",
		description = "이메일이 이미 존재함",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"이미 존재하는 이메일입니다.\", \"errorCode\": \"EMAIL_ALREADY_EXISTS\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@ApiResponse(
		responseCode = "422",
		description = "이메일 인증이 완료되지 않음",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"이메일 인증이 완료되지 않았습니다.\", \"errorCode\": \"EMAIL_NOT_VERIFIED\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@PostMapping("/register")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
		log.info("POST /api/auth/register - Email: {}", request.getEmail());
		authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(com.skax.physicalrisk.dto.common.ApiResponse.success("회원가입이 완료되었습니다."));
	}

	/**
	 * 로그인
	 *
	 * @param request 로그인 요청 (email: 이메일 주소, password: 비밀번호)
	 * @return 토큰 및 사용자 정보 (accessToken: 액세스 토큰, refreshToken: 리프레시 토큰, userId: 사용자 ID)
	 * @throws UnauthorizedException 이메일이 존재하지 않거나 비밀번호가 일치하지 않는 경우 (401)
	 */
	@Operation(
		summary = "로그인"
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "로그인 정보",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = LoginRequest.class),
			examples = @ExampleObject(
				value = "{\"email\": \"user@example.com\", \"password\": \"password123!\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "200",
		description = "로그인 성공 및 토큰 반환",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				value = "{\"result\": \"success\", \"message\": \"로그인에 성공했습니다.\", \"data\": {\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"userId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"}}"
			)
		)
	)
	@ApiResponse(
		responseCode = "401",
		description = "인증 실패 (이메일 또는 비밀번호 불일치)",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"이메일 또는 비밀번호가 일치하지 않습니다.\", \"errorCode\": \"INVALID_CREDENTIALS\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@PostMapping("/login")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		log.info("POST /api/auth/login - Email: {}", request.getEmail());
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success("로그인에 성공했습니다.", response));
	}

	/**
	 * 로그아웃
	 *
	 * @return 성공 메시지
	 * @throws UnauthorizedException 인증되지 않은 사용자인 경우 (401)
	 */
	@Operation(
		summary = "로그아웃",
		description = "현재 로그인된 사용자를 로그아웃합니다. RefreshToken을 무효화합니다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "로그아웃 성공",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(value = "{\"result\": \"success\", \"message\": \"로그아웃되었습니다.\"}")
		)
	)
	@ApiResponse(
		responseCode = "401",
		description = "인증되지 않은 사용자",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"인증되지 않은 사용자입니다.\", \"errorCode\": \"UNAUTHORIZED\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@PostMapping("/logout")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<Void>> logout() {
		log.info("POST /api/auth/logout");
		String userId = SecurityUtil.getCurrentUserId().toString();
		authService.logout(userId);
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success("로그아웃되었습니다."));
	}

	/**
	 * 토큰 갱신
	 *
	 * @param request 리프레시 토큰 요청 (refreshToken: 리프레시 토큰)
	 * @return 갱신된 액세스 토큰 및 리프레시 토큰 (accessToken: 새 액세스 토큰, refreshToken: 새 리프레시 토큰, userId: 사용자 ID)
	 * @throws UnauthorizedException 리프레시 토큰이 유효하지 않거나 만료된 경우 (401)
	 */
	@Operation(
		summary = "토큰 재발급"
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "리프레시 토큰",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = RefreshTokenRequest.class),
			examples = @ExampleObject(
				value = "{\"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "200",
		description = "새로운 액세스/리프레시 토큰 반환",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				value = "{\"result\": \"success\", \"message\": \"토큰이 갱신되었습니다.\", \"data\": {\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"userId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"}}"
			)
		)
	)
	@ApiResponse(
		responseCode = "401",
		description = "리프레시 토큰이 유효하지 않거나 만료됨",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"리프레시 토큰이 유효하지 않거나 만료되었습니다.\", \"errorCode\": \"INVALID_REFRESH_TOKEN\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@PostMapping("/refresh")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		log.info("POST /api/auth/refresh");
		LoginResponse response = authService.refresh(request.getRefreshToken());
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success("토큰이 갱신되었습니다.", response));
	}

	/**
	 * 비밀번호 재설정 이메일 인증 요청
	 *
	 * @param request 비밀번호 재설정 이메일 요청 (email: 이메일 주소)
	 * @return 성공 메시지
	 * @throws ResourceNotFoundException 이메일이 존재하지 않는 경우 (404)
	 * @throws BusinessException 이메일 발송에 실패한 경우 (503)
	 */
	@Operation(
		summary = "비밀번호 재설정 이메일 발송",
		description = "비밀번호 재설정을 위한 6자리 인증번호를 이메일로 발송합니다."
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "이메일 주소",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = PasswordResetEmailRequest.class),
			examples = @ExampleObject(
				value = "{\"email\": \"user@example.com\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "200",
		description = "이메일 발송 성공",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(value = "{\"result\": \"success\", \"message\": \"비밀번호 재설정 인증번호가 이메일로 발송되었습니다.\"}")
		)
	)
	@ApiResponse(
		responseCode = "404",
		description = "이메일이 존재하지 않음",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"존재하지 않는 이메일입니다.\", \"errorCode\": \"USER_NOT_FOUND\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@ApiResponse(
		responseCode = "503",
		description = "이메일 발송 실패",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"이메일 발송에 실패했습니다.\", \"errorCode\": \"EMAIL_SEND_FAILED\", \"timestamp\": \"2025-12-11T15:30:00\"}")
		)
	)
	@PostMapping("/password/reset-email")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<Void>> resetPasswordEmail(@Valid @RequestBody PasswordResetEmailRequest request) {
		log.info("POST /api/auth/password/reset-email - Email: {}", request.getEmail());
		authService.sendPasswordResetEmail(request.getEmail());
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success("비밀번호 재설정 인증번호가 이메일로 발송되었습니다."));
	}
}
