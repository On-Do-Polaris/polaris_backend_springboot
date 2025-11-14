package com.skax.physicalrisk.dto.response.user;

import com.skax.physicalrisk.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 응답 DTO
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

	private UUID id;
	private String email;
	private String name;
	private String organization;
	private String language;
	private User.UserRole role;
	private LocalDateTime createdAt;
	private LocalDateTime lastLoginAt;

	/**
	 * 엔티티로부터 응답 DTO 생성
	 *
	 * @param user 사용자 엔티티
	 * @return UserResponse
	 */
	public static UserResponse from(User user) {
		return UserResponse.builder()
			.id(user.getId())
			.email(user.getEmail())
			.name(user.getName())
			.organization(user.getOrganization())
			.language(user.getLanguage())
			.role(user.getRole())
			.createdAt(user.getCreatedAt())
			.lastLoginAt(user.getLastLoginAt())
			.build();
	}
}
