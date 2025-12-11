package com.skax.physicalrisk.config;

import com.skax.physicalrisk.security.CustomAccessDeniedHandler;
import com.skax.physicalrisk.security.CustomAuthenticationEntryPoint;
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

import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정
 *
 * 최종 수정일: 2025-12-11
 * 파일 버전: v03
 *
 * JWT 기반 인증 및 권한 설정
 * - 인증 실패 시 401 Unauthorized 반환 (CustomAuthenticationEntryPoint)
 * - 권한 부족 시 403 Forbidden 반환 (CustomAccessDeniedHandler)
 *
 * @author SKAX Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;

	@Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8080}")
	private String allowedOrigins;

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
				.requestMatchers("/", "/index.html", "/*.css", "/*.js", "/*.png", "/*.ico").permitAll() // 정적 파일 허용
				.anyRequest().authenticated() // 나머지는 인증 필요
			)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(customAuthenticationEntryPoint) // 401 처리
				.accessDeniedHandler(customAccessDeniedHandler) // 403 처리
			)
			.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // H2 콘솔용
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가

		return http.build();
	}

	/**
	 * CORS 설정
	 * 환경변수 CORS_ALLOWED_ORIGINS로 허용 도메인 설정 가능
	 * 예: CORS_ALLOWED_ORIGINS=http://localhost:3000,https://your-domain.com
	 *
	 * @return CorsConfigurationSource
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// 허용할 origin (환경변수에서 읽어옴)
		List<String> origins = Arrays.asList(allowedOrigins.split(","));
		configuration.setAllowedOrigins(origins);

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
