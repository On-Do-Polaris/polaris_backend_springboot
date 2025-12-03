package com.skax.physicalrisk.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh Token 엔티티
 *
 * 최종 수정일: 2025-12-03
 * 파일 버전: v01
 *
 * JWT Refresh Token 관리 (DB 기반)
 * ERD 문서 기준 스키마를 따름
 *
 * @author SKAX Team
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id; // 토큰 고유 ID

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user; // 사용자

	@Column(name = "token", unique = true, nullable = false, length = 500)
	private String token; // Refresh Token 값

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt; // 만료 일시

	@Column(name = "created_at", nullable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now(); // 생성 일시

	@Column(name = "revoked", nullable = false)
	@Builder.Default
	private Boolean revoked = false; // 폐기 여부

	@Column(name = "device_info", length = 255)
	private String deviceInfo; // 디바이스 정보

	@Column(name = "ip_address", length = 45)
	private String ipAddress; // IP 주소

	/**
	 * 토큰 만료 여부 확인
	 *
	 * @return 만료되었으면 true
	 */
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}

	/**
	 * 토큰 유효성 확인
	 *
	 * @return 유효하면 true
	 */
	public boolean isValid() {
		return !revoked && !isExpired();
	}

	/**
	 * 토큰 폐기
	 */
	public void revoke() {
		this.revoked = true;
	}
}
