package com.skax.physicalrisk.service.user;

import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.dto.request.user.UpdateUserRequest;
import com.skax.physicalrisk.dto.response.user.UserResponse;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import com.skax.physicalrisk.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 사용자 서비스
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;

	/**
	 * 현재 사용자 정보 조회
	 *
	 * @return 사용자 정보
	 */
	public UserResponse getCurrentUser() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Fetching current user: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		return UserResponse.builder()
			.email(user.getEmail())
			.name(user.getName())
			.language(user.getLanguage())
			.build();
	}

	/**
	 * 사용자 정보 수정
	 *
	 * @param request 수정 요청
	 * @return 수정된 사용자 정보
	 */
	@Transactional
	public UserResponse updateUser(UpdateUserRequest request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Updating user: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		// 언어 설정만 수정 가능
		if (request.getLanguage() != null) {
			user.setLanguage(request.getLanguage());
		}

		User savedUser = userRepository.save(user);
		log.info("User updated successfully: {}", userId);

		return UserResponse.builder()
			.email(savedUser.getEmail())
			.name(savedUser.getName())
			.language(savedUser.getLanguage())
			.build();
	}

	/**
	 * 사용자 삭제 (비활성화)
	 */
	@Transactional
	public void deleteUser() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Deleting user: {}", userId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		user.deactivate();
		userRepository.save(user);

		log.info("User deleted successfully: {}", userId);
	}
}
