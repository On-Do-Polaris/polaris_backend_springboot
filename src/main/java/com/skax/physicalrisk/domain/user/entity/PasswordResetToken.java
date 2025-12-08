package com.skax.physicalrisk.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 비밀번호 재설정 토큰 엔티티
 *
 * 최종 수정일: 2025-12-08
 * 파일 버전: v02 - ERD 기준 수정 (token 길이, JPA Auditing, 인덱스)
 *
 * 비밀번호 재설정 시 이메일로 발송된 토큰 정보 관리
 * ERD 문서 기준 스키마를 따름
 *
 * @author SKAX Team
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
	@Index(name = "idx_password_reset_user_id", columnList = "user_id"),
	@Index(name = "idx_password_reset_token", columnList = "token")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id; // 토큰 고유 ID

	@Column(name = "token", unique = true, nullable = false, length = 255)
	private String token; // 재설정 토큰

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user; // 사용자

	@CreatedDate
	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt; // 생성 일시

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt; // 만료 일시

	@Column(name = "used", nullable = false)
	@Builder.Default
	private Boolean used = false; // 사용 여부

	/**
	 * 토큰 만료 여부 확인
	 *
	 * @return 만료되었으면 true
	 */
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}

	/**
	 * 토큰 사용 처리
	 */
	public void markAsUsed() {
		this.used = true;
	}

	/**
	 * 토큰 유효성 검증
	 *
	 * @return 유효하면 true
	 */
	public boolean isValid() {
		return !used && !isExpired();
	}
}
