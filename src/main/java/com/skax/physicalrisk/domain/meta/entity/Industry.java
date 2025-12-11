package com.skax.physicalrisk.domain.meta.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * 업종 엔티티
 *
 * 최종 수정일: 2025-12-11
 * 파일 버전: v02
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
@Schema(description = "산업 분류")
public class Industry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@Schema(description = "산업 ID", example = "1")
	private Long id;

	@Column(name = "code", unique = true, nullable = false, length = 50)
	@Schema(description = "산업 코드", example = "data_center")
	private String code;

	@Column(name = "name", nullable = false, length = 100)
	@Schema(description = "산업 이름", example = "데이터센터")
	private String name;

	@Column(name = "description", columnDefinition = "TEXT")
	@Schema(description = "산업 설명", example = "서버 및 IT 인프라 운영 시설")
	private String description;
}
