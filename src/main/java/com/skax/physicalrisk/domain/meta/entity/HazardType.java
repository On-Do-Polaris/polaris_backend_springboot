package com.skax.physicalrisk.domain.meta.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 기후 위험 요인 유형 엔티티
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * 시스템에서 지원하는 기후 위험 요인 메타 정보
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
public class HazardType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id; // 고유 ID

	@Column(name = "code", unique = true, nullable = false, length = 50)
	private String code; // 코드 (예: extreme_heat)

	@Column(name = "name", nullable = false, length = 100)
	private String name; // 한글 이름

	@Column(name = "name_en", length = 100)
	private String nameEn; // 영문 이름

	@Enumerated(EnumType.STRING)
	@Column(name = "category", length = 20)
	private HazardCategory category; // 카테고리

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
