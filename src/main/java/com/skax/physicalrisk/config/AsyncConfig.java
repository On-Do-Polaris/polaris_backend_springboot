package com.skax.physicalrisk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 설정
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * 분석 작업 비동기 처리용 설정
 *
 * @author SKAX Team
 */
@Configuration
@EnableAsync
public class AsyncConfig {

	/**
	 * 비동기 작업용 Executor
	 *
	 * @return Executor
	 */
	@Bean(name = "taskExecutor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("Async-");
		executor.initialize();
		return executor;
	}
}
