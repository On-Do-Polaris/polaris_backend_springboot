package com.skax.physicalrisk.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * 사용자 엔티티
 *
 * 최종 수정일: 2025-12-03
 * 파일 버전: v02 - ERD 스키마 맞춤 단순화
 *
 * 사용자 인증 및 프로필 정보를 관리하는 도메인 엔티티
 * ERD 문서 기준 스키마를 따름
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
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id; // 사용자 고유 ID

	@Column(name = "email", unique = true, nullable = false, length = 255)
	private String email; // 이메일 (로그인 ID)

	@Column(name = "name", nullable = false, length = 100)
	private String name; // 사용자 이름

	@Column(name = "password", nullable = false, length = 255)
	private String password; // 비밀번호 (암호화됨)

	@Column(name = "language", length = 10)
	@Builder.Default
	private String language = "ko"; // 언어 설정 (ko, en)

	/**
	 * 비밀번호 업데이트 (비밀번호 재설정용)
	 */
	public void updatePassword(String encodedPassword) {
		this.password = encodedPassword;
	}
}
