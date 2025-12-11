package com.skax.physicalrisk.domain.meta.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * 기후 위험 요인 유형 엔티티
 *
 * 최종 수정일: 2025-12-11
 * 파일 버전: v03 - Swagger 어노테이션 추가
 *
 * 시스템에서 지원하는 기후 위험 요인 메타 정보
 * ERD 문서 기준 스키마를 따름
 *
 * @author SKAX Team
 */
@Entity
@Table(name = "hazard_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "재해 유형")
public class HazardType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@Schema(description = "재해 유형 ID", example = "1")
	private Long id;

	@Column(name = "code", unique = true, nullable = false, length = 50)
	@Schema(description = "재해 유형 코드", example = "extreme_heat")
	private String code;

	@Column(name = "name", nullable = false, length = 100)
	@Schema(description = "재해 유형 한글 이름", example = "극심한 고온")
	private String name;

	@Column(name = "name_en", length = 100)
	@Schema(description = "재해 유형 영문 이름", example = "Extreme Heat")
	private String nameEn;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", length = 20)
	@Schema(description = "재해 카테고리", example = "TEMPERATURE")
	private HazardCategory category;

	@Column(name = "description", columnDefinition = "TEXT")
	@Schema(description = "재해 유형 설명", example = "폭염 및 열파로 인한 위험")
	private String description;

	/**
	 * 위험 요인 카테고리 열거형
	 */
	public enum HazardCategory {
		TEMPERATURE, // 온도 관련 (극심한 고온, 극심한 한파)
		WATER,       // 물 관련 (하천 홍수, 도시 홍수, 가뭄, 물부족, 해수면 상승)
		WIND,        // 바람 관련 (태풍)
		OTHER        // 기타 (산불)
	}
}
