package com.skax.physicalrisk.constants;

/**
 * 기후 위험 유형 상수
 *
 * 리스크 명칭 통일 기준:
 * - 한글 명칭: 사용자 UI에 표시되는 한글 이름
 * - 영문 코드: 데이터베이스 컬럼, 변수명, API 통신에 사용되는 영문 코드 (snake_case)
 * - 영문 명칭: 영문 UI에 표시되는 영문 이름 (Title Case)
 *
 * 최종 수정일: 2025-11-21
 *
 * @author SKAX Team
 */
public final class HazardTypeConstants {

	private HazardTypeConstants() {
		// 유틸리티 클래스는 인스턴스화 방지
	}

	// 영문 코드 (데이터베이스/API용)
	public static final String EXTREME_HEAT = "extreme_heat";
	public static final String EXTREME_COLD = "extreme_cold";
	public static final String WILDFIRE = "wildfire";
	public static final String DROUGHT = "drought";
	public static final String WATER_STRESS = "water_stress";
	public static final String SEA_LEVEL_RISE = "sea_level_rise";
	public static final String RIVER_FLOOD = "river_flood";
	public static final String URBAN_FLOOD = "urban_flood";
	public static final String TYPHOON = "typhoon";

	// 한글 명칭 (UI 표시용)
	public static final String EXTREME_HEAT_KR = "극심한 고온";
	public static final String EXTREME_COLD_KR = "극심한 한파";
	public static final String WILDFIRE_KR = "산불";
	public static final String DROUGHT_KR = "가뭄";
	public static final String WATER_STRESS_KR = "물부족";
	public static final String SEA_LEVEL_RISE_KR = "해수면 상승";
	public static final String RIVER_FLOOD_KR = "하천 홍수";
	public static final String URBAN_FLOOD_KR = "도시 홍수";
	public static final String TYPHOON_KR = "태풍";

	// 영문 명칭 (영문 UI 표시용)
	public static final String EXTREME_HEAT_EN = "Extreme Heat";
	public static final String EXTREME_COLD_EN = "Extreme Cold";
	public static final String WILDFIRE_EN = "Wildfire";
	public static final String DROUGHT_EN = "Drought";
	public static final String WATER_STRESS_EN = "Water Stress";
	public static final String SEA_LEVEL_RISE_EN = "Sea Level Rise";
	public static final String RIVER_FLOOD_EN = "River Flood";
	public static final String URBAN_FLOOD_EN = "Urban Flood";
	public static final String TYPHOON_EN = "Typhoon";
}
