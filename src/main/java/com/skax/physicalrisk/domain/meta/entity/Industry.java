package com.skax.physicalrisk.domain.meta.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 업종 엔티티
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * 시스템에서 지원하는 업종 메타 정보
 *
 * @author SKAX Team
 */
@Entity
@Table(name = "industries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Industry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id; // 고유 ID

	@Column(name = "code", unique = true, nullable = false, length = 50)
	private String code; // 코드 (예: data_center)

	@Column(name = "name", nullable = false, length = 100)
	private String name; // 업종 이름

	@Column(name = "description", columnDefinition = "TEXT")
	private String description; // 설명
}
