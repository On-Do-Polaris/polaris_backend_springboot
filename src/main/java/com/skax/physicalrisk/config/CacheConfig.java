package com.skax.physicalrisk.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 캐시 설정
 *
 * FastAPI 분석 결과 캐싱용
 * 단일 서버 환경에 최적화된 인메모리 캐시
 *
 * @author SKAX Team
 */
@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		cacheManager.setCaffeine(Caffeine.newBuilder()
			.maximumSize(500)
			.expireAfterWrite(30, TimeUnit.MINUTES)
			.recordStats());
		return cacheManager;
	}
}
