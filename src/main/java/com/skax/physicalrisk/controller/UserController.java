package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.user.UpdateUserRequest;
import com.skax.physicalrisk.dto.response.ErrorResponse;
import com.skax.physicalrisk.dto.response.user.UserResponse;
import com.skax.physicalrisk.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 사용자 컨트롤러
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	/**
	 * 현재 사용자 정보 조회
	 *
	 * @return 사용자 정보
	 */
	@Operation(
		summary = "사용자 정보 조회",
		description = "사용자 페이지에서 사용할 현재 로그인 사용자 정보 조회."
	)
	@ApiResponse(
		responseCode = "200",
		description = "사용자 정보",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = UserResponse.class),
			examples = @ExampleObject(
				value = "{\"email\": \"user@example.com\", \"name\": \"홍길동\", \"language\": \"ko\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "401",
		description = "인증되지 않은 사용자",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"인증이 필요합니다\", \"errorCode\": \"UNAUTHORIZED\", \"code\": \"UNAUTHORIZED\", \"timestamp\": \"2025-12-17T15:30:00.123456789\"}")
		)
	)
	@GetMapping("/me")
	public ResponseEntity<UserResponse> getCurrentUser() {
		log.info("GET /api/users/me - Fetching current user");
		UserResponse response = userService.getCurrentUser();
		return ResponseEntity.ok(response);
	}

	/**
	 * 사용자 정보 수정
	 *
	 * @param request 수정 요청
	 * @return 수정된 사용자 정보
	 */
	@Operation(
		summary = "사용자 정보 수정",
		description = "사용자 정보 중 언어 정보를 수정한다."
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "수정할 사용자 정보",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = UpdateUserRequest.class),
			examples = @ExampleObject(
				value = "{\"language\": \"ko\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "200",
		description = "수정된 사용자 정보",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = UserResponse.class),
			examples = @ExampleObject(
				value = "{\"email\": \"user@example.com\", \"name\": \"홍길동\", \"language\": \"ko\"}"
			)
		)
	)
	@ApiResponse(
		responseCode = "401",
		description = "인증되지 않은 사용자",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"인증이 필요합니다\", \"errorCode\": \"UNAUTHORIZED\", \"code\": \"UNAUTHORIZED\", \"timestamp\": \"2025-12-17T15:30:00.123456789\"}")
		)
	)
	@PatchMapping("/me")
	public ResponseEntity<UserResponse> updateUser(@RequestBody UpdateUserRequest request) {
		log.info("PATCH /api/users/me - Updating user");
		UserResponse response = userService.updateUser(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 사용자 삭제 (비활성화)
	 *
	 * @return 성공 메시지
	 */
	@Operation(
		summary = "계정 삭제",
		description = "계정을 삭제하는 엔드포인트."
	)
	@ApiResponse(
		responseCode = "200",
		description = "계정 삭제 결과 반환",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = com.skax.physicalrisk.dto.common.ApiResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"success\", \"message\": \"계정이 삭제되었습니다.\"}")
		)
	)
	@ApiResponse(
		responseCode = "401",
		description = "인증되지 않은 사용자",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"인증이 필요합니다\", \"errorCode\": \"UNAUTHORIZED\", \"code\": \"UNAUTHORIZED\", \"timestamp\": \"2025-12-17T15:30:00.123456789\"}")
		)
	)
	@DeleteMapping("/me")
	public ResponseEntity<com.skax.physicalrisk.dto.common.ApiResponse<Void>> deleteUser() {
		log.info("DELETE /api/users/me - Deleting user");
		userService.deleteUser();
		return ResponseEntity.ok(com.skax.physicalrisk.dto.common.ApiResponse.success("계정이 삭제되었습니다."));
	}
}
