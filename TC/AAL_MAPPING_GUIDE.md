# AAL API 재해 종류 매핑 가이드

## 개요
`/api/analysis/aal` 엔드포인트는 FastAPI로부터 AAL(Average Annual Loss) 데이터를 받아서 클라이언트에게 전달합니다.
FastAPI가 사용하는 재해 종류 명칭과 Spring Boot가 사용하는 표준 한글 명칭이 다르기 때문에, 이를 매핑하는 로직이 구현되어 있습니다.

## 재해 종류 매핑 (9가지)

| FastAPI riskType | Spring Boot hazardType | 영문명 | 설명 |
|------------------|------------------------|--------|------|
| 가뭄 | 가뭄 | Drought | 가뭄 |
| 한파 | 극심한 저온 | Cold Wave | 한파 |
| 폭염 | 극심한 고온 | Heat Wave | 폭염 |
| 내륙침수 | 하천 홍수 | Inland Flood | 내륙침수 |
| 해안침수 | 해수면 상승 | Coastal Flood | 해안침수 |
| 태풍 | 태풍 | Typhoon | 태풍 |
| 도시침수 | 도시 홍수 | Urban Flood | 도시침수 |
| 물부족 | 물 부족 | Water Scarcity | 물부족 |
| 산불 | 산불 | Wildfire | 산불 |

## 시나리오 매핑

FastAPI는 4가지 SSP(Shared Socioeconomic Pathways) 시나리오를 제공합니다:

| 시나리오 코드 | 설명 | 응답 필드 |
|--------------|------|----------|
| SSP1-2.6 | 지속가능한 발전 경로 (저탄소) | scenarios1 |
| SSP2-4.5 | 중간 경로 | scenarios2 |
| SSP3-7.0 | 지역 분열 경로 (고탄소) | scenarios3 |
| SSP5-8.5 | 화석연료 집약 발전 경로 (최고탄소) | scenarios4 |

## 기간(Term) 매핑

| Term 값 | 설명 | 데이터 포인트 |
|---------|------|--------------|
| short | 단기 (2026년) | point1 (1개) |
| mid | 중기 (2026~2030년) | point1~5 (5개) |
| long | 장기 (2020s~2050s) | point1~4 (4개) |

## FastAPI 응답 예시

```json
{
  "scenarios": [
    {
      "scenario": "SSP1-2.6",
      "riskType": "폭염",
      "shortTerm": {
        "point1": 0.00707435
      },
      "midTerm": {
        "point1": 0.00707435,
        "point2": 0.00811052,
        "point3": 0.00735511,
        "point4": 0.00867113,
        "point5": 0.01105575
      },
      "longTerm": {
        "point1": 0,
        "point2": 0,
        "point3": 0,
        "point4": 0
      }
    },
    {
      "scenario": "SSP2-4.5",
      "riskType": "폭염",
      "shortTerm": {
        "point1": 0.01558847
      },
      "midTerm": {
        "point1": 0.01558847,
        "point2": 0.01713493,
        "point3": 0.01677061,
        "point4": 0.01470857,
        "point5": 0.02081786
      },
      "longTerm": {
        "point1": 0,
        "point2": 0,
        "point3": 0,
        "point4": 0
      }
    }
  ]
}
```

## Spring Boot 응답 예시

```json
{
  "result": "success",
  "data": {
    "siteId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "term": "mid",
    "hazardType": "극심한 고온",
    "scenarios1": {
      "point1": 0.00707435,
      "point2": 0.00811052,
      "point3": 0.00735511,
      "point4": 0.00867113,
      "point5": 0.01105575
    },
    "scenarios2": {
      "point1": 0.01558847,
      "point2": 0.01713493,
      "point3": 0.01677061,
      "point4": 0.01470857,
      "point5": 0.02081786
    },
    "scenarios3": {
      "point1": 0.01911098,
      "point2": 0.02169913,
      "point3": 0.02564433,
      "point4": 0.02200999,
      "point5": 0.0252426
    },
    "scenarios4": {
      "point1": 0.02372254,
      "point2": 0.02920655,
      "point3": 0.03693806,
      "point4": 0.03773896,
      "point5": 0.04057882
    },
    "reason": "폭염으로 인한 냉방 비용 증가, 작업 효율 저하에 따른 생산성 감소"
  }
}
```

## 매핑 로직 처리 위치

### 1. HazardTypeMapper.java
- 위치: `src/main/java/com/skax/physicalrisk/util/HazardTypeMapper.java`
- 역할: FastAPI riskType과 Spring Boot hazardType 간 양방향 매핑
- 주요 메서드:
  - `toStandardKorean(String fastApiRiskType)`: FastAPI → Spring Boot 변환
  - `toFastApiKorean(String standardKorean)`: Spring Boot → FastAPI 변환
  - `matches(String hazardType1, String hazardType2)`: 두 값이 같은 재해인지 비교

### 2. AnalysisService.java
- 위치: `src/main/java/com/skax/physicalrisk/service/analysis/AnalysisService.java`
- 메서드: `getFinancialImpact(UUID siteId, String hazardType, String term)`
- 역할:
  1. FastAPI로부터 모든 시나리오 데이터 수신
  2. `HazardTypeMapper.matches()`를 사용하여 요청된 hazardType과 일치하는 riskType 필터링
  3. term에 맞는 데이터 추출
  4. 4개 시나리오별로 데이터 분류 및 응답 생성

### 3. FinancialImpactResponse.java
- 위치: `src/main/java/com/skax/physicalrisk/dto/response/analysis/FinancialImpactResponse.java`
- 역할:
  - FastAPI 응답 구조(`scenarios` 배열)를 내부 DTO로 매핑
  - 최종 클라이언트 응답 형식 정의
  - **중요**: AAL 값은 `Double` 타입 (소수점 값)

## API 호출 예시

### 요청
```http
GET /api/analysis/aal?siteId=3fa85f64-5717-4562-b3fc-2c963f66afa6&term=mid&hazardType=극심한%20고온
```

### 내부 처리 흐름
1. AnalysisController가 요청 수신
2. AnalysisService.getFinancialImpact() 호출
3. FastApiClient.getFinancialImpact() 호출
   - FastAPI 엔드포인트: `GET /api/analysis/financial-impacts?siteId={siteId}&hazardType={hazardType}&term={term}`
4. FastAPI로부터 모든 시나리오 데이터 수신 (9개 재해 × 4개 시나리오 = 최대 36개 ScenarioData)
5. HazardTypeMapper를 사용하여 "극심한 고온"과 매칭되는 "폭염" 데이터만 필터링
6. term="mid"에 해당하는 midTerm 데이터 추출
7. 4개 시나리오별로 분류하여 scenarios1~4에 할당
8. FinancialImpactResponse 생성 및 반환

## 주의사항

1. **타입 변경**: AAL 값은 `Integer`가 아닌 `Double` 타입입니다 (예: 0.00707435)
2. **자동 매핑**: HazardTypeMapper가 "극심한 고온"과 "폭염"을 자동으로 매칭합니다
3. **제로 값 처리**: 모든 포인트가 0인 경우 해당 시나리오는 제외됩니다
4. **용어 일관성**: 클라이언트는 항상 Spring Boot 표준 한글명("극심한 고온")을 사용해야 합니다

## 디버깅

로그 레벨을 DEBUG로 설정하면 상세한 매핑 과정을 확인할 수 있습니다:

```yaml
logging:
  level:
    com.skax.physicalrisk.service.analysis: DEBUG
    com.skax.physicalrisk.util: DEBUG
```

주요 로그 메시지:
- `Processing financial scenario: {scenario}, riskType: {riskType}, requested hazardType: {hazardType}`
- `Skipping financial scenario due to riskType mismatch`
- `Found valid financial termData for {term}: {count} points`
- `Converted FinancialImpactResponse: {response}`

## 관련 파일

1. `AnalysisController.java` - API 엔드포인트 정의
2. `AnalysisService.java` - 비즈니스 로직 및 매핑 처리
3. `FinancialImpactResponse.java` - 응답 DTO
4. `HazardTypeMapper.java` - 재해 종류 매핑 유틸
5. `FastApiClient.java` - FastAPI 통신 클라이언트

## 버전 히스토리

- v03 (2025-12-19): AAL 값 타입을 Double로 변경, 재해 종류 매핑 문서화
- v02 (2025-11-20): HazardTypeMapper 기반 스마트 매칭 적용
- v01 (2025-11-18): 최초 구현
