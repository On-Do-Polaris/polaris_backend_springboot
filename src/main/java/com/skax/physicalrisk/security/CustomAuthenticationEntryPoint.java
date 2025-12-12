package com.skax.physicalrisk.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skax.physicalrisk.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 인증되지 않은 사용자의 접근을 처리하는 핸들러
 *
 * Spring Security에서 인증이 필요한 리소스에 인증되지 않은 사용자가 접근할 때 호출됩니다.
 * 기본적으로 403이 발생하는 것을 401로 변경합니다.
 *
 * 최종 수정일: 2025-12-12
 * 파일 버전: v02 (ObjectMapper 주입으로 LocalDateTime 직렬화 문제 해결)
 *
 * @author SKAX Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
						 AuthenticationException authException) throws IOException, ServletException {
		log.error("Unauthorized access attempt: {}", authException.getMessage());

		// 401 Unauthorized 응답 설정
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		// ErrorResponse 생성
		ErrorResponse errorResponse = ErrorResponse.builder()
			.result("error")
			.message("인증되지 않은 사용자입니다.")
			.errorCode("UNAUTHORIZED")
			.code("UNAUTHORIZED")
			.timestamp(LocalDateTime.now())
			.build();

		// JSON 응답 작성
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
