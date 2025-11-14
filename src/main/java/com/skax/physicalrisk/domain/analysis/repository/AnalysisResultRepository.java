package com.skax.physicalrisk.domain.analysis.repository;

import com.skax.physicalrisk.domain.analysis.entity.AnalysisResult;
import com.skax.physicalrisk.domain.site.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 분석 결과 레포지토리
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {

	/**
	 * 사업장의 모든 분석 결과 조회
	 *
	 * @param site 사업장
	 * @return 분석 결과 리스트
	 */
	List<AnalysisResult> findBySite(Site site);

	/**
	 * 사업장의 특정 위험 요인 분석 결과 조회
	 *
	 * @param site 사업장
	 * @param hazardType 위험 요인 유형
	 * @return 분석 결과 Optional
	 */
	Optional<AnalysisResult> findBySiteAndHazardType(Site site, String hazardType);

	/**
	 * 사업장의 최근 분석 결과 조회
	 *
	 * @param site 사업장
	 * @return 분석 결과 Optional
	 */
	Optional<AnalysisResult> findFirstBySiteOrderByAnalyzedAtDesc(Site site);
}
