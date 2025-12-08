package com.skax.physicalrisk.domain.site.entity;

import com.skax.physicalrisk.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * 사업장 엔티티
 *
 * 최종 수정일: 2025-12-08
 * 파일 버전: v03 - ERD 기준 수정 (좌표 precision/scale, 인덱스)
 *
 * 기후 리스크 분석 대상 사업장 정보 관리
 * ERD 문서 기준 스키마를 따름
 *
 * @author SKAX Team
 */
@Entity
@Table(name = "sites", indexes = {
	@Index(name = "idx_site_user_id", columnList = "user_id"),
	@Index(name = "idx_site_coordinates", columnList = "latitude, longitude")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id; // 사업장 고유 ID

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user; // 소유 사용자

	@Column(name = "name", nullable = false, length = 255)
	private String name; // 사업장 이름

	@Column(name = "road_address", length = 500)
	private String roadAddress; // 도로명 주소

	@Column(name = "jibun_address", length = 500)
	private String jibunAddress; // 지번 주소

	@Column(name = "latitude", precision = 10, scale = 8)
	private Double latitude; // 위도 (decimal(10,8))

	@Column(name = "longitude", precision = 11, scale = 8)
	private Double longitude; // 경도 (decimal(11,8))

	@Column(name = "type", length = 100)
	private String type; // 업종/유형
}
