package com.skax.physicalrisk.domain.analysis.entity;

import com.skax.physicalrisk.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * AI 분석 작업 엔티티
 *
 * 최종 수정일: 2025-12-03
 * 파일 버전: v02 - ERD 스키마 맞춤 단순화
 *
 * FastAPI AI Agent 분석 작업 상태 관리
 * ERD 문서 기준 스키마를 따름
 *
 * @author SKAX Team
 */
@Entity
@Table(name = "analysis_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisJob {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id; // 작업 고유 ID

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_id", nullable = false)
	private Site site; // 분석 대상 사업장

	@Column(name = "job_id", unique = true, nullable = false, length = 100)
	private String jobId; // FastAPI 작업 ID

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private JobStatus status = JobStatus.QUEUED; // 작업 상태

	@Column(name = "progress")
	@Builder.Default
	private Integer progress = 0; // 진행률 (0-100)

	@Column(name = "current_node", length = 100)
	private String currentNode; // 현재 처리 중인 노드

	@Column(name = "error_code", length = 50)
	private String errorCode; // 에러 코드

	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage; // 에러 메시지

	/**
	 * 작업 상태 열거형
	 */
	public enum JobStatus {
		QUEUED,    // 대기 중
		RUNNING,   // 실행 중
		COMPLETED, // 완료
		FAILED     // 실패
	}

	/**
	 * 진행률 업데이트
	 *
	 * @param progress 진행률 (0-100)
	 * @param currentNode 현재 노드
	 */
	public void updateProgress(Integer progress, String currentNode) {
		this.progress = progress;
		this.currentNode = currentNode;
	}

	/**
	 * 작업 실패 처리
	 *
	 * @param errorCode 에러 코드
	 * @param errorMessage 에러 메시지
	 */
	public void fail(String errorCode, String errorMessage) {
		this.status = JobStatus.FAILED;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
}
