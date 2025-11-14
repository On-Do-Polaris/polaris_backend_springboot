package com.skax.physicalrisk.domain.report.entity;

import com.skax.physicalrisk.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 리포트 엔티티
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * 사업장 리스크 분석 리포트 정보 관리
 *
 * @author SKAX Team
 */
@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id; // 리포트 고유 ID

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_id", nullable = false)
	private Site site; // 대상 사업장

	@Column(name = "site_name", length = 200)
	private String siteName; // 사업장 이름 (스냅샷)

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private ReportType type; // 리포트 유형

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private ReportStatus status = ReportStatus.PENDING; // 리포트 상태

	@Column(name = "s3_key", length = 500)
	private String s3Key; // S3 저장 키

	@Column(name = "file_size")
	private Long fileSize; // 파일 크기 (bytes)

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt; // 생성 일시

	@Column(name = "completed_at")
	private LocalDateTime completedAt; // 완료 일시

	@Column(name = "expires_at")
	private LocalDateTime expiresAt; // 다운로드 만료 일시

	/**
	 * 리포트 유형 열거형
	 */
	public enum ReportType {
		SUMMARY,    // 요약 리포트
		FULL,       // 전체 리포트
		GOVERNANCE  // 거버넌스 리포트 (TCFD)
	}

	/**
	 * 리포트 상태 열거형
	 */
	public enum ReportStatus {
		PENDING,    // 대기 중
		GENERATING, // 생성 중
		COMPLETED,  // 완료
		FAILED      // 실패
	}

	/**
	 * 리포트 생성 완료 처리
	 *
	 * @param s3Key S3 키
	 * @param fileSize 파일 크기
	 */
	public void complete(String s3Key, Long fileSize) {
		this.status = ReportStatus.COMPLETED;
		this.s3Key = s3Key;
		this.fileSize = fileSize;
		this.completedAt = LocalDateTime.now();
		this.expiresAt = LocalDateTime.now().plusDays(7); // 7일 후 만료
	}

	/**
	 * 리포트 생성 실패 처리
	 */
	public void fail() {
		this.status = ReportStatus.FAILED;
		this.completedAt = LocalDateTime.now();
	}

	/**
	 * 다운로드 만료 여부 확인
	 *
	 * @return 만료되었으면 true
	 */
	public boolean isExpired() {
		return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
	}
}
