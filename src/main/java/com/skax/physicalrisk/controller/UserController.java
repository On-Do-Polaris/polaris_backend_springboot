package com.skax.physicalrisk.controller;

import com.skax.physicalrisk.dto.request.user.UpdateUserRequest;
import com.skax.physicalrisk.dto.response.user.UserResponse;
import com.skax.physicalrisk.service.user.UserService;
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
	@DeleteMapping("/me")
	public ResponseEntity<Map<String, Object>> deleteUser() {
		log.info("DELETE /api/users/me - Deleting user");
		userService.deleteUser();
		return ResponseEntity.ok(java.util.Collections.emptyMap());
	}

	/**
	 * 비밀번호 변경 (미구현)
	 *
	 * @return 성공 메시지
	 */
	@PatchMapping("/me/password")
	public ResponseEntity<Map<String, String>> changePassword() {
		log.info("PATCH /api/users/me/password - Not implemented yet");
		return ResponseEntity.ok(Map.of("message", "Not implemented yet"));
	}
}
