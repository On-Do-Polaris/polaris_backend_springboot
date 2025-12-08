package com.skax.physicalrisk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정
 *
 * 최종 수정일: 2025-12-08
 * 파일 버전: v01
 *
 * Entity의 생성/수정 시간을 자동으로 관리하기 위한 JPA Auditing 활성화
 * - @CreatedDate: 생성 시간 자동 설정
 * - @LastModifiedDate: 수정 시간 자동 업데이트
 *
 * @author SKAX Team
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
	// JPA Auditing 활성화
	// @CreatedDate, @LastModifiedDate 어노테이션이 자동으로 동작합니다.
}
