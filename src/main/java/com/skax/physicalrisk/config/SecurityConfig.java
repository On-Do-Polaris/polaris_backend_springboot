package com.skax.physicalrisk.config;

import com.skax.physicalrisk.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * JWT 기반 인증 및 권한 설정
 *
 * @author SKAX Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	/**
	 * Security Filter Chain 설정
	 *
	 * @param http HttpSecurity
	 * @return SecurityFilterChain
	 * @throws Exception 예외
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화 (JWT 사용)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/health/**").permitAll() // 헬스 체크는 모두 허용
				.requestMatchers("/api/auth/**").permitAll() // 인증 API는 모두 허용
				.requestMatchers("/api/meta/**").permitAll() // 메타 API는 모두 허용
				.requestMatchers("/swagger-ui/**", "/v3/api-docs.yaml",  "/v3/api-docs/**").permitAll() // Swagger는 모두 허용
				.requestMatchers("/h2-console/**").permitAll() // H2 콘솔은 모두 허용 (개발용)
				.anyRequest().authenticated() // 나머지는 인증 필요
			)
			.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // H2 콘솔용
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가

		return http.build();
	}

	/**
	 * 비밀번호 암호화
	 *
	 * @return PasswordEncoder
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
