package com.skax.physicalrisk.domain.analysis.entity;

import com.skax.physicalrisk.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * AI 분석 결과 엔티티
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * FastAPI로부터 받은 분석 결과를 JSON 형태로 저장
 *
 * @author SKAX Team
 */
@Entity
@Table(name = "analysis_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id; // 결과 고유 ID

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_id", nullable = false)
	private Site site; // 분석 대상 사업장

	@Column(name = "hazard_type", length = 50)
	private String hazardType; // 위험 요인 유형

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "analysis_data", columnDefinition = "JSON")
	private Map<String, Object> analysisData; // 분석 결과 데이터 (JSON)

	@CreationTimestamp
	@Column(name = "analyzed_at", updatable = false)
	private LocalDateTime analyzedAt; // 분석 일시

	/**
	 * 특정 키의 데이터 조회
	 *
	 * @param key 데이터 키
	 * @return 데이터 값
	 */
	public Object getData(String key) {
		return analysisData != null ? analysisData.get(key) : null;
	}

	/**
	 * 데이터 추가/업데이트
	 *
	 * @param key 데이터 키
	 * @param value 데이터 값
	 */
	public void putData(String key, Object value) {
		if (analysisData != null) {
			analysisData.put(key, value);
		}
	}
}
