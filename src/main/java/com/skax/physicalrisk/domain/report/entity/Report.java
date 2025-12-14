package com.skax.physicalrisk.domain.report.entity;

import com.skax.physicalrisk.domain.user.entity.User;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.Map;
import java.util.UUID;

/**
 * 리포트 엔티티
 *
 * 최종 수정일: 2025-12-14
 * 파일 버전: v04 - Application (4).dbml 기준 수정
 *
 * 사용자별 AI 분석 리포트 저장
 * DBML 문서 기준 스키마를 따름
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
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id; // 리포트 고유 ID

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user; // 사용자 ID

	@Type(JsonBinaryType.class)
	@Column(name = "report_content", columnDefinition = "jsonb")
	private Map<String, Object> reportContent; // 리포트 내용 메타데이터 (JSONB)
}
