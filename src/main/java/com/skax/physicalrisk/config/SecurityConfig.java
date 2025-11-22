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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
			.cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
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
	 * CORS 설정
	 *
	 * @return CorsConfigurationSource
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// 허용할 origin (프론트엔드 도메인)
		configuration.setAllowedOrigins(Arrays.asList(
			"http://localhost:3000",  // React 개발 서버
			"http://localhost:5173",  // Vite 개발 서버
			"http://localhost:8080",  // Vue CLI 개발 서버
			"http://localhost:8081",  // Vue CLI 대체 포트
			"https://skax.co.kr",     // 프로덕션 도메인
			"https://www.skax.co.kr"
		));

		// 허용할 HTTP 메서드
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

		// 허용할 헤더
		configuration.setAllowedHeaders(Arrays.asList("*"));

		// 인증 정보 포함 허용
		configuration.setAllowCredentials(true);

		// preflight 요청 캐시 시간 (1시간)
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
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
