package com.skax.physicalrisk.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 엔티티
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * 사용자 인증 및 프로필 정보를 관리하는 도메인 엔티티
 *
 * @author SKAX Team
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id; // 사용자 고유 ID

	@Column(name = "email", unique = true, nullable = false, length = 255)
	private String email; // 이메일 (로그인 ID)

	@Column(name = "name", nullable = false, length = 100)
	private String name; // 사용자 이름

	@Column(name = "password", nullable = false, length = 255)
	private String password; // 비밀번호 (암호화됨)

	@Column(name = "organization", length = 200)
	private String organization; // 소속 조직

	@Column(name = "language", length = 10)
	@Builder.Default
	private String language = "ko"; // 언어 설정 (ko, en)

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	@Builder.Default
	private UserRole role = UserRole.USER; // 사용자 권한

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt; // 생성 일시

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt; // 수정 일시

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt; // 마지막 로그인 일시

	/**
	 * 사용자 권한 열거형
	 */
	public enum UserRole {
		USER,  // 일반 사용자
		ADMIN  // 관리자
	}

	/**
	 * 마지막 로그인 시간 업데이트
	 */
	public void updateLastLogin() {
		this.lastLoginAt = LocalDateTime.now();
	}
}
