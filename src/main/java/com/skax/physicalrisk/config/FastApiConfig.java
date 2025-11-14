package com.skax.physicalrisk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * FastAPI WebClient 설정
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Configuration
public class FastApiConfig {

	@Value("${fastapi.base-url}")
	private String baseUrl;

	/**
	 * FastAPI WebClient Bean
	 *
	 * @return WebClient
	 */
	@Bean
	public WebClient fastApiWebClient() {
		return WebClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.codecs(configurer -> configurer
				.defaultCodecs()
				.maxInMemorySize(16 * 1024 * 1024)) // 16MB
			.build();
	}
}
