# FastAPI 엔드포인트 구현 요청

## 개요
Spring Boot 백엔드에서 FastAPI 호출 코드가 구현되었습니다.
아래 5개 엔드포인트를 FastAPI 측에서 구현해주시기 바랍니다.

---

## 1. 분석 개요 조회
**엔드포인트**: `GET /api/analysis/summary`

**요청 파라미터**:
- `siteId` (UUID, required): 사업장 ID

**응답 예시**:
```json
{
  "mainClimateRisk": "태풍",
  "mainClimateRiskScore": 70,
  "mainClimateRiskAAL": 17,
  "physical-risk-scores": [
    {
      "extreme_heat": 10,
      "extreme_cold": 20,
      "river_flood": 30,
      "urban_flood": 40,
      "drought": 50,
      "water_stress": 60,
      "sea_level_rise": 50,
      "typhoon": 70,
      "wildfire": 60
    }
  ],
  "aal-scores": [
    {
      "extreme_heat": 9,
      "extreme_cold": 10,
      "river_flood": 11,
      "urban_flood": 12,
      "drought": 13,
      "water_stress": 14,
      "sea_level_rise": 15,
      "typhoon": 17,
      "wildfire": 16
    }
  ]
}
```

**호출 위치**: `AnalysisService.getAnalysisSummary()`

---

## 2. 위치 시뮬레이션 후보지 조회
**엔드포인트**: `GET /api/simulation/location/recommendation`

**요청 파라미터**:
- `siteId` (String, required): 사업장 ID

**응답**:
- `RelocationSimulationResponse` 형식
- 추천 후보지 3개 및 각 후보지의 리스크 정보

**호출 위치**: `SimulationService.getLocationRecommendation()`

---

## 3. 통합 리포트 조회
**엔드포인트**: `GET /api/reports`

**요청 파라미터**:
- `userId` (UUID, required): 사용자 ID

**응답 예시**:
```json
{
  "ceosummry": "CEO 요약 내용",
  "Governance": "거버넌스 내용",
  "strategy": "전략 내용",
  "riskmanagement": "리스크 관리 내용",
  "goal": "목표 내용"
}
```

**호출 위치**: `ReportService.getReport()`

---

## 4. 리포트 추가 데이터 등록
**엔드포인트**: `POST /api/reports/data`

**요청 Body**:
```json
{
  "userId": "uuid-string",
  "siteId": "uuid-string",
  "data": {
    // 추가 데이터 필드들
  }
}
```

**응답**:
- 등록 결과 (성공/실패)

**호출 위치**: `ReportService.registerReportData()`

---

## 5. 과거 재해 이력 조회
**엔드포인트**: `GET /api/past`

**요청 파라미터**:
- `year` (int, required): 연도
- `disasterType` (String, required): 재해 유형
- `severity` (String, required): 심각도

**응답**:
- `PastDisasterResponse` 형식
- 과거 재해 이력 목록

**호출 위치**: `PastDisasterService.getPastDisasters()`

---

## 공통 사항

### 헤더
모든 요청에 `X-API-Key` 헤더가 포함됩니다:
```
X-API-Key: {api-key}
```

### 응답 형식
모든 응답은 `Map<String, Object>` 형식으로 반환됩니다.

### 에러 처리
- 연결 실패 시: Spring Boot에서 `BusinessException`으로 변환
- 에러 코드: `ErrorCode.FASTAPI_CONNECTION_ERROR`

---

## 구현 완료 파일

### FastApiClient.java
- 5개 메서드 추가 완료:
  - `getAnalysisSummary(UUID siteId)`
  - `getLocationRecommendation(String siteId)`
  - `getReportByUserId(UUID userId)`
  - `registerReportData(Map<String, Object> request)`
  - `getPastDisasters(int year, String disasterType, String severity)`

### Service 파일
- `AnalysisService.java` - TODO 주석 해제 및 FastAPI 호출 활성화
- `SimulationService.java` - TODO 주석 해제 및 FastAPI 호출 활성화
- `ReportService.java` - 2개 메서드 TODO 주석 해제 및 FastAPI 호출 활성화
- `PastDisasterService.java` - TODO 주석 해제 및 FastAPI 호출 활성화

### 빌드 상태
✅ **BUILD SUCCESS** (125 files compiled)

---

## 다음 단계

1. FastAPI 팀에서 위 5개 엔드포인트 구현
2. 엔드포인트 경로 및 응답 형식 확인
3. 통합 테스트 진행
4. 필요시 응답 형식 조정

---

**작성일**: 2025-12-11
**작성자**: Backend Team (Spring Boot)
**FastAPI 구현 담당**: FastAPI Team
