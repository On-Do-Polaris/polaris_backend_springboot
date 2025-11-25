package com.skax.physicalrisk.config;

import com.skax.physicalrisk.domain.meta.entity.HazardType;
import com.skax.physicalrisk.domain.meta.entity.Industry;
import com.skax.physicalrisk.domain.meta.repository.HazardTypeRepository;
import com.skax.physicalrisk.domain.meta.repository.IndustryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 데이터베이스 초기 데이터 로딩
 *
 * 애플리케이션 시작 시 필수 메타데이터를 자동으로 로드합니다.
 * - Industries (업종)
 * - HazardTypes (기후 위험 유형)
 *
 * 최종 수정일: 2025-11-24
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

	private final IndustryRepository industryRepository;
	private final HazardTypeRepository hazardTypeRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		log.info("Starting database initialization...");

		initializeIndustries();
		initializeHazardTypes();

		log.info("Database initialization completed.");
	}

	/**
	 * 업종 초기 데이터 로드
	 */
	private void initializeIndustries() {
		if (industryRepository.count() > 0) {
			log.info("Industries already initialized. Skipping...");
			return;
		}

		log.info("Initializing Industries...");

		List<Industry> industries = Arrays.asList(
			Industry.builder()
				.code("data_center")
				.name("데이터센터")
				.description("서버, 네트워크 장비, 스토리지 등 IT 인프라 운영 시설")
				.build(),

			Industry.builder()
				.code("manufacturing")
				.name("제조업")
				.description("제품 생산 및 가공 시설 (전자, 기계, 화학 등)")
				.build(),

			Industry.builder()
				.code("logistics")
				.name("물류/창고")
				.description("물류 센터, 배송 센터, 창고 시설")
				.build(),

			Industry.builder()
				.code("retail")
				.name("유통/판매")
				.description("대형마트, 백화점, 쇼핑몰 등 유통 시설")
				.build(),

			Industry.builder()
				.code("office")
				.name("사무/오피스")
				.description("사무실, 본사, 지사 등 사무 공간")
				.build(),

			Industry.builder()
				.code("healthcare")
				.name("의료/복지")
				.description("병원, 요양원, 의료 시설")
				.build(),

			Industry.builder()
				.code("education")
				.name("교육")
				.description("학교, 연구소, 교육 시설")
				.build(),

			Industry.builder()
				.code("energy")
				.name("에너지/발전")
				.description("발전소, 변전소, 에너지 생산 시설")
				.build(),

			Industry.builder()
				.code("finance")
				.name("금융")
				.description("은행, 증권, 보험 등 금융 기관")
				.build(),

			Industry.builder()
				.code("hospitality")
				.name("숙박/관광")
				.description("호텔, 리조트, 관광 시설")
				.build(),

			Industry.builder()
				.code("agriculture")
				.name("농업/축산")
				.description("농장, 축산 시설, 온실")
				.build(),

			Industry.builder()
				.code("chemical")
				.name("화학/정유")
				.description("화학 공장, 정유 시설")
				.build(),

			Industry.builder()
				.code("food")
				.name("식품/음료")
				.description("식품 가공, 음료 제조 시설")
				.build(),

			Industry.builder()
				.code("pharmaceutical")
				.name("제약/바이오")
				.description("제약 공장, 바이오 연구 시설")
				.build(),

			Industry.builder()
				.code("transportation")
				.name("교통/운송")
				.description("공항, 항만, 철도, 버스 터미널 등")
				.build(),

			Industry.builder()
				.code("other")
				.name("기타")
				.description("기타 업종")
				.build()
		);

		industryRepository.saveAll(industries);
		log.info("Successfully initialized {} industries", industries.size());
	}

	/**
	 * 기후 위험 유형 초기 데이터 로드
	 * ERD 다이어그램 기준: 8개 주요 위험 요인
	 */
	private void initializeHazardTypes() {
		if (hazardTypeRepository.count() > 0) {
			log.info("HazardTypes already initialized. Skipping...");
			return;
		}

		log.info("Initializing HazardTypes...");

		List<HazardType> hazardTypes = Arrays.asList(
			HazardType.builder()
				.code("extreme_heat")
				.name("극심한 고온")
				.nameEn("Extreme Heat")
				.category(HazardType.HazardCategory.TEMPERATURE)
				.description("이상 고온 및 폭염으로 인한 위험")
				.build(),

			HazardType.builder()
				.code("extreme_cold")
				.name("극심한 한파")
				.nameEn("Extreme Cold")
				.category(HazardType.HazardCategory.TEMPERATURE)
				.description("이상 저온 및 한파로 인한 위험")
				.build(),

			HazardType.builder()
				.code("river_flood")
				.name("하천 홍수")
				.nameEn("River Flood")
				.category(HazardType.HazardCategory.WATER)
				.description("하천 범람으로 인한 침수 위험")
				.build(),

			HazardType.builder()
				.code("urban_flood")
				.name("도시 홍수")
				.nameEn("Urban Flood")
				.category(HazardType.HazardCategory.WATER)
				.description("도시 내 집중호우로 인한 침수 위험")
				.build(),

			HazardType.builder()
				.code("drought")
				.name("가뭄")
				.nameEn("Drought")
				.category(HazardType.HazardCategory.WATER)
				.description("강수 부족으로 인한 가뭄 위험")
				.build(),

			HazardType.builder()
				.code("water_stress")
				.name("물 부족")
				.nameEn("Water Stress")
				.category(HazardType.HazardCategory.WATER)
				.description("용수 공급 부족으로 인한 위험")
				.build(),

			HazardType.builder()
				.code("sea_level_rise")
				.name("해수면 상승")
				.nameEn("Sea Level Rise")
				.category(HazardType.HazardCategory.WATER)
				.description("해수면 상승으로 인한 침수 위험")
				.build(),

			HazardType.builder()
				.code("typhoon")
				.name("태풍")
				.nameEn("Typhoon")
				.category(HazardType.HazardCategory.WIND)
				.description("강풍과 폭우를 동반한 태풍 위험")
				.build(),

			HazardType.builder()
				.code("wildfire")
				.name("산불")
				.nameEn("Wildfire")
				.category(HazardType.HazardCategory.OTHER)
				.description("산림 화재로 인한 위험")
				.build()
		);

		hazardTypeRepository.saveAll(hazardTypes);
		log.info("Successfully initialized {} hazard types", hazardTypes.size());
	}
}
