package com.skax.physicalrisk.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 이메일 인증 코드 엔티티
 *
 * 최종 수정일: 2025-12-10
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Entity
@Table(
	name = "verification_codes",
	indexes = {
		@Index(name = "idx_verification_email_purpose", columnList = "email, purpose, verified")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VerificationCode {

	/**
	 * 인증 코드 ID (PK)
	 */
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
	private UUID id;

	/**
	 * 인증 대상 이메일
	 */
	@Column(name = "email", nullable = false, length = 255)
	private String email;

	/**
	 * 6자리 인증번호
	 */
	@Column(name = "code", nullable = false, length = 6)
	private String code;

	/**
	 * 인증 목적 (REGISTER, PASSWORD_RESET)
	 */
	@Column(name = "purpose", nullable = false, length = 20)
	private String purpose;

	/**
	 * 인증 완료 여부
	 */
	@Column(name = "verified", nullable = false)
	@Builder.Default
	private boolean verified = false;

	/**
	 * 만료 시간 (생성 시점 + 5분)
	 */
	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	/**
	 * 생성 시간
	 */
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/**
	 * 엔티티 생성 전 자동 설정
	 */
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		if (this.expiresAt == null) {
			this.expiresAt = this.createdAt.plusMinutes(5);
		}
	}

	/**
	 * 인증 완료 처리
	 */
	public void verify() {
		this.verified = true;
	}

	/**
	 * 만료 여부 확인
	 *
	 * @return 만료된 경우 true
	 */
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(this.expiresAt);
	}

	/**
	 * 인증 목적 상수
	 */
	public static class Purpose {
		public static final String REGISTER = "REGISTER";
		public static final String PASSWORD_RESET = "PASSWORD_RESET";
	}
}
