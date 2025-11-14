package com.skax.physicalrisk.domain.analysis.repository;

import com.skax.physicalrisk.domain.analysis.entity.AnalysisJob;
import com.skax.physicalrisk.domain.site.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 분석 작업 레포지토리
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Repository
public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, UUID> {

	/**
	 * FastAPI 작업 ID로 조회
	 *
	 * @param jobId FastAPI 작업 ID
	 * @return 작업 Optional
	 */
	Optional<AnalysisJob> findByJobId(String jobId);

	/**
	 * 사업장의 최근 작업 조회
	 *
	 * @param site 사업장
	 * @return 작업 Optional
	 */
	Optional<AnalysisJob> findFirstBySiteOrderByCreatedAtDesc(Site site);

	/**
	 * 사업장의 실행 중인 작업 조회
	 *
	 * @param site 사업장
	 * @param status 작업 상태
	 * @return 작업 리스트
	 */
	List<AnalysisJob> findBySiteAndStatus(Site site, AnalysisJob.JobStatus status);

	/**
	 * 사업장의 실행 중인 작업 존재 여부
	 *
	 * @param site 사업장
	 * @param status 작업 상태
	 * @return 존재하면 true
	 */
	boolean existsBySiteAndStatus(Site site, AnalysisJob.JobStatus status);
}
