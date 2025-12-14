package com.skax.physicalrisk.domain.report.repository;

import com.skax.physicalrisk.domain.report.entity.Report;
import com.skax.physicalrisk.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 리포트 레포지토리
 *
 * 최종 수정일: 2025-12-14
 * 파일 버전: v02 - Application (4).dbml 기준 수정
 *
 * @author SKAX Team
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

	/**
	 * 사용자의 리포트 조회
	 *
	 * @param user 사용자
	 * @return 리포트 (Optional)
	 */
	Optional<Report> findByUser(User user);
}
