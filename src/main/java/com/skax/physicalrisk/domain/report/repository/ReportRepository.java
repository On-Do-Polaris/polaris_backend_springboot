package com.skax.physicalrisk.domain.report.repository;

import com.skax.physicalrisk.domain.report.entity.Report;
import com.skax.physicalrisk.domain.site.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 리포트 레포지토리
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

	/**
	 * 사업장의 리포트 목록 조회
	 *
	 * @param site 사업장
	 * @param pageable 페이징 정보
	 * @return 리포트 페이지
	 */
	Page<Report> findBySite(Site site, Pageable pageable);

	/**
	 * 만료된 리포트 조회
	 *
	 * @param now 현재 시간
	 * @return 리포트 리스트
	 */
	List<Report> findByExpiresAtBefore(LocalDateTime now);
}
