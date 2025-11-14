package com.skax.physicalrisk.service;

import com.skax.physicalrisk.domain.meta.entity.HazardType;
import com.skax.physicalrisk.domain.meta.entity.Industry;
import com.skax.physicalrisk.domain.meta.repository.HazardTypeRepository;
import com.skax.physicalrisk.domain.meta.repository.IndustryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 메타 데이터 서비스
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetaService {

	private final HazardTypeRepository hazardTypeRepository;
	private final IndustryRepository industryRepository;

	/**
	 * 모든 위험 유형 조회
	 *
	 * @return 위험 유형 목록
	 */
	public List<HazardType> getAllHazardTypes() {
		log.info("Fetching all hazard types");
		return hazardTypeRepository.findAll();
	}

	/**
	 * 모든 산업 분류 조회
	 *
	 * @return 산업 분류 목록
	 */
	public List<Industry> getAllIndustries() {
		log.info("Fetching all industries");
		return industryRepository.findAll();
	}
}
