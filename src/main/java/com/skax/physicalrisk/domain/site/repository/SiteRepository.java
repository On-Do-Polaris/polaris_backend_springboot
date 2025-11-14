package com.skax.physicalrisk.domain.site.repository;

import com.skax.physicalrisk.domain.site.entity.Site;
import com.skax.physicalrisk.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 사업장 레포지토리
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Repository
public interface SiteRepository extends JpaRepository<Site, UUID> {

	/**
	 * 사용자의 사업장 목록 조회
	 *
	 * @param user 사용자
	 * @param pageable 페이징 정보
	 * @return 사업장 페이지
	 */
	Page<Site> findByUser(User user, Pageable pageable);

	/**
	 * 사용자의 사업장 ID로 조회
	 *
	 * @param id 사업장 ID
	 * @param user 사용자
	 * @return 사업장 Optional
	 */
	Optional<Site> findByIdAndUser(UUID id, User user);

	/**
	 * 사용자의 사업장 검색 (이름 또는 주소)
	 *
	 * @param user 사용자
	 * @param keyword 검색 키워드
	 * @param pageable 페이징 정보
	 * @return 사업장 페이지
	 */
	@Query("SELECT s FROM Site s WHERE s.user = :user AND (s.name LIKE %:keyword% OR s.address LIKE %:keyword%)")
	Page<Site> searchByUserAndKeyword(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

	/**
	 * 사용자의 리스크 레벨별 사업장 조회
	 *
	 * @param user 사용자
	 * @param riskLevel 리스크 레벨
	 * @param pageable 페이징 정보
	 * @return 사업장 페이지
	 */
	Page<Site> findByUserAndRiskLevel(User user, Site.RiskLevel riskLevel, Pageable pageable);

	/**
	 * 사용자의 업종별 사업장 조회
	 *
	 * @param user 사용자
	 * @param industry 업종
	 * @param pageable 페이징 정보
	 * @return 사업장 페이지
	 */
	Page<Site> findByUserAndIndustry(User user, String industry, Pageable pageable);

	/**
	 * 사용자의 전체 사업장 수
	 *
	 * @param user 사용자
	 * @return 사업장 수
	 */
	long countByUser(User user);

	/**
	 * 사용자의 리스크 레벨별 사업장 수
	 *
	 * @param user 사용자
	 * @param riskLevel 리스크 레벨
	 * @return 사업장 수
	 */
	long countByUserAndRiskLevel(User user, Site.RiskLevel riskLevel);
}
