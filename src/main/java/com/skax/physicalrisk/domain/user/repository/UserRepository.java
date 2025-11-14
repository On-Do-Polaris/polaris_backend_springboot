package com.skax.physicalrisk.domain.user.repository;

import com.skax.physicalrisk.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 레포지토리
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	/**
	 * 이메일로 사용자 조회
	 *
	 * @param email 이메일
	 * @return 사용자 Optional
	 */
	Optional<User> findByEmail(String email);

	/**
	 * 이메일 존재 여부 확인
	 *
	 * @param email 이메일
	 * @return 존재하면 true
	 */
	boolean existsByEmail(String email);
}
