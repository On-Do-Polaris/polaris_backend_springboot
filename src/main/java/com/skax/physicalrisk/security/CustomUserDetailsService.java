package com.skax.physicalrisk.security;

import com.skax.physicalrisk.domain.user.entity.User;
import com.skax.physicalrisk.domain.user.repository.UserRepository;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

/**
 * Spring Security UserDetailsService 구현
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	/**
	 * 이메일로 사용자 정보 조회
	 *
	 * @param email 이메일
	 * @return UserDetails
	 * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
	 */
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

		return createUserDetails(user);
	}

	/**
	 * 사용자 ID로 사용자 정보 조회
	 *
	 * @param userId 사용자 ID
	 * @return UserDetails
	 */
	@Transactional(readOnly = true)
	public UserDetails loadUserByUserId(UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

		return createUserDetails(user);
	}

	/**
	 * UserDetails 객체 생성
	 *
	 * @param user 사용자 엔티티
	 * @return UserDetails
	 */
	private UserDetails createUserDetails(User user) {
		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getId().toString())
			.password(user.getPassword())
			.authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
			.accountExpired(false)
			.accountLocked(false)
			.credentialsExpired(false)
			.disabled(!user.isActive())
			.build();
	}
}
