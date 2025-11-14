package com.skax.physicalrisk.domain.meta.repository;

import com.skax.physicalrisk.domain.meta.entity.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 업종 레포지토리
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Repository
public interface IndustryRepository extends JpaRepository<Industry, Long> {

	/**
	 * 코드로 조회
	 *
	 * @param code 코드
	 * @return 업종 Optional
	 */
	Optional<Industry> findByCode(String code);
}
