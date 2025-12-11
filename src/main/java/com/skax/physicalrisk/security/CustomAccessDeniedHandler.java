package com.skax.physicalrisk.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skax.physicalrisk.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 권한이 없는 사용자의 접근을 처리하는 핸들러
 *
 * Spring Security에서 인증된 사용자가 권한이 없는 리소스에 접근할 때 호출됩니다.
 * 403 Forbidden 응답을 반환합니다.
 *
 * 최종 수정일: 2025-12-11
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
					   AccessDeniedException accessDeniedException) throws IOException, ServletException {
		log.error("Access denied: {}", accessDeniedException.getMessage());

		// 403 Forbidden 응답 설정
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		// ErrorResponse 생성
		ErrorResponse errorResponse = ErrorResponse.builder()
			.result("error")
			.message("접근 권한이 없습니다.")
			.errorCode("ACCESS_DENIED")
			.code("ACCESS_DENIED")
			.timestamp(LocalDateTime.now())
			.build();

		// JSON 응답 작성
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
