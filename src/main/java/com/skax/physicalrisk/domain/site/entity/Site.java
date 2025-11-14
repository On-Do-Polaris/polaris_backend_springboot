package com.skax.physicalrisk.domain.site.entity;

import com.skax.physicalrisk.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사업장 엔티티
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * 기후 리스크 분석 대상 사업장 정보 관리
 *
 * @author SKAX Team
 */
@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id; // 사업장 고유 ID

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user; // 소유 사용자

	@Column(name = "name", nullable = false, length = 200)
	private String name; // 사업장 이름

	@Column(name = "address", nullable = false, length = 500)
	private String address; // 주소

	@Column(name = "city", length = 100)
	private String city; // 도시

	@Column(name = "latitude")
	private Double latitude; // 위도

	@Column(name = "longitude")
	private Double longitude; // 경도

	@Column(name = "industry", length = 100)
	private String industry; // 업종

	@Column(name = "description", columnDefinition = "TEXT")
	private String description; // 사업장 설명

	@Column(name = "main_hazard", length = 50)
	private String mainHazard; // 주요 리스크 유형

	@Column(name = "risk_score")
	private Integer riskScore; // 종합 리스크 점수 (0-100)

	@Enumerated(EnumType.STRING)
	@Column(name = "risk_level", length = 20)
	private RiskLevel riskLevel; // 리스크 레벨

	@Column(name = "last_analyzed_at")
	private LocalDateTime lastAnalyzedAt; // 마지막 분석 일시

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt; // 생성 일시

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt; // 수정 일시

	/**
	 * 리스크 레벨 열거형
	 */
	public enum RiskLevel {
		LOW("낮음"),
		MODERATE("보통"),
		HIGH("높음"),
		CRITICAL("심각");

		private final String koreanName;

		RiskLevel(String koreanName) {
			this.koreanName = koreanName;
		}

		public String getKoreanName() {
			return koreanName;
		}
	}

	/**
	 * 분석 결과 업데이트
	 *
	 * @param mainHazard 주요 리스크
	 * @param riskScore 리스크 점수
	 */
	public void updateAnalysisResult(String mainHazard, Integer riskScore) {
		this.mainHazard = mainHazard;
		this.riskScore = riskScore;
		this.riskLevel = calculateRiskLevel(riskScore);
		this.lastAnalyzedAt = LocalDateTime.now();
	}

	/**
	 * 리스크 점수에 따른 레벨 계산
	 *
	 * @param score 리스크 점수
	 * @return 리스크 레벨
	 */
	private RiskLevel calculateRiskLevel(Integer score) {
		if (score == null) {
			return null;
		}
		if (score < 30) {
			return RiskLevel.LOW;
		} else if (score < 60) {
			return RiskLevel.MODERATE;
		} else if (score < 80) {
			return RiskLevel.HIGH;
		} else {
			return RiskLevel.CRITICAL;
		}
	}
}
