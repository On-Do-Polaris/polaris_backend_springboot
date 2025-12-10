# FastAPI ↔ Spring Boot API 불일치 분석

**분석 일자**: 2025-12-10
**분석 대상**: Spring Boot FastApiClient.java vs FastAPI OpenAPI spec (docs/openapi.json)

---

## 🚨 심각한 불일치 사항

### 1. 분석(Analysis) 엔드포인트 - 경로 변수 사용 (CRITICAL)

#### 문제: FastAPI가 경로 변수를 모두 제거했으나 Spring Boot는 여전히 사용 중

| 엔드포인트 | Spring Boot 현재 | FastAPI 실제 | 상태 |
|-----------|-----------------|-------------|------|
| 분석 시작 | `/api/sites/{siteId}/analysis/start` | `/api/analysis/start` | ❌ BROKEN |
| 분석 상태 | `/api/sites/{siteId}/analysis/status/{jobId}` | `/api/analysis/status?siteId=...&jobId=...` | ❌ BROKEN |
| 리스크 점수 | `/api/sites/{siteId}/analysis/physical-risk-scores` | `/api/analysis/physical-risk-scores?siteId=...` | ❌ BROKEN |
| 과거 이벤트 | `/api/sites/{siteId}/analysis/past-events` | `/api/analysis/past-events?siteId=...` | ❌ BROKEN |
| SSP 전망 | `/api/sites/{siteId}/analysis/ssp` | `/api/analysis/ssp?siteId=...` (추정) | ❌ BROKEN |
| 재무 영향 | `/api/sites/{siteId}/analysis/financial-impacts` | `/api/analysis/financial-impacts?siteId=...` | ❌ BROKEN |
| 취약성 | `/api/sites/{siteId}/analysis/vulnerability` | `/api/analysis/vulnerability?siteId=...` | ❌ BROKEN |
| 통합 분석 | `/api/sites/{siteId}/analysis/total` | `/api/analysis/total?siteId=...&hazardType=...` | ❌ BROKEN |

**영향도**: 🔴 HIGH - 모든 분석 API 호출 실패 (404 Not Found)

---

### 2. 추가 데이터(Additional Data) 엔드포인트 - 경로 변수 사용 (CRITICAL)

| 엔드포인트 | Spring Boot 현재 | FastAPI 실제 | 상태 |
|-----------|-----------------|-------------|------|
| 업로드 | `/api/sites/{siteId}/additional-data` | `/api/additional-data` (body에 siteId) | ❌ BROKEN |
| 조회 | `/api/sites/{siteId}/additional-data?dataCategory=...` | `/api/additional-data?siteId=...&dataCategory=...` | ❌ BROKEN |
| 삭제 | `/api/sites/{siteId}/additional-data/{dataId}` | `/api/additional-data?siteId=...&dataCategory=...` | ❌ BROKEN |
| 정형화 조회 | `/api/sites/{siteId}/additional-data/{dataId}/structured` | (OpenAPI에 없음) | ❌ BROKEN |

**영향도**: 🔴 HIGH - 모든 추가 데이터 API 호출 실패 (404 Not Found)

---

### 3. 후보지 추천 배치 - 파라미터 이름 불일치 (CRITICAL)

#### 문제: Spring Boot는 `batchJobId` 사용, FastAPI는 `batchId` 기대

**FastApiClient.java:**
```java
public Mono<Map<String, Object>> getBatchProgress(UUID batchJobId) {
    return webClient.get()
        .uri("/api/recommendation/{batchJobId}/progress", batchJobId)  // ❌ 경로 변수 사용!
        ...
}

public Mono<Map<String, Object>> getRecommendationResult(UUID batchJobId) {
    return webClient.get()
        .uri("/api/recommendation/{batchJobId}/result", batchJobId)  // ❌ 경로 변수 사용!
        ...
}
```

**FastAPI OpenAPI:**
```json
{
  "path": "/api/recommendation/batch/progress",
  "parameters": [
    {
      "name": "batchId",  // ⚠️ batchId (not batchJobId)
      "in": "query",       // ⚠️ query parameter (not path variable)
      "required": true
    }
  ]
}
```

| 메서드 | Spring Boot 현재 | FastAPI 실제 | 상태 |
|-------|-----------------|-------------|------|
| 배치 시작 | `/api/recommendation` | `/api/recommendation/batch/start` | ❌ BROKEN |
| 진행 상태 | `/api/recommendation/{batchJobId}/progress` | `/api/recommendation/batch/progress?batchId=...` | ❌ BROKEN |
| 결과 조회 | `/api/recommendation/{batchJobId}/result` | `/api/recommendation/batch/result?batchId=...` | ❌ BROKEN |

**영향도**: 🔴 HIGH - 배치 작업 상태/결과 조회 불가 (404 Not Found or 422 Validation Error)

---

### 4. 리포트 엔드포인트 - 파라미터 이름 불일치

#### 문제: Spring Boot는 `userId` 사용, FastAPI는 `reportId` 기대

**FastApiClient.java:**
```java
public Mono<Map<String, Object>> getReportWebViewByUserId(UUID userId) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/api/reports/web")
            .queryParam("userId", userId)  // ❌ userId
            .build())
}
```

**FastAPI OpenAPI:**
```json
{
  "path": "/api/reports/web",
  "parameters": [
    {
      "name": "reportId",  // ⚠️ reportId (not userId)
      "in": "query",
      "required": true
    }
  ]
}
```

| 메서드 | Spring Boot 현재 | FastAPI 실제 | 상태 |
|-------|-----------------|-------------|------|
| 웹 뷰 조회 | `?userId=...` | `?reportId=...` | ❌ BROKEN |
| PDF 조회 | `?userId=...` | `?reportId=...` | ❌ BROKEN |
| 삭제 | `?userId=...` | (DELETE /api/reports, 파라미터 없음) | ❌ BROKEN |

**영향도**: 🟡 MEDIUM - 리포트 조회 실패 (422 Validation Error)

---

### 5. 대시보드 엔드포인트 - 경로 불일치

| 메서드 | Spring Boot 현재 | FastAPI 실제 | 상태 |
|-------|-----------------|-------------|------|
| 요약 조회 | `/api/v1/dashboard/summary?userId=...` | `/api/dashboard/summary` (no userId) | ❌ BROKEN |

**영향도**: 🟡 MEDIUM - 대시보드 조회 실패

---

## ✅ 정상 작동하는 엔드포인트

| 엔드포인트 | Spring Boot | FastAPI | 상태 |
|-----------|-------------|---------|------|
| 사업장 이전 시뮬레이션 | `/api/simulation/relocation/compare` | `/api/simulation/relocation/compare` | ✅ OK |
| 기후 시뮬레이션 | `/api/simulation/climate` | `/api/simulation/climate` | ✅ OK |
| 리포트 생성 | `/api/reports` | `/api/reports` | ✅ OK |
| 재해 이력 조회 | `/api/disaster-history` | `/api/disaster-history` | ✅ OK |

---

## 🔧 필요한 수정 사항 요약

### 우선순위 1: 경로 변수 제거 (CRITICAL)

1. **Analysis 엔드포인트 8개**
   - 모든 `/api/sites/{siteId}/analysis/*` → `/api/analysis/*`
   - siteId를 query parameter로 변경

2. **Additional Data 엔드포인트 4개**
   - 모든 `/api/sites/{siteId}/additional-data/*` → `/api/additional-data`
   - siteId를 query parameter 또는 body로 변경

3. **Recommendation 엔드포인트 3개**
   - `/api/recommendation` → `/api/recommendation/batch/start`
   - `/api/recommendation/{batchJobId}/progress` → `/api/recommendation/batch/progress?batchId=...`
   - `/api/recommendation/{batchJobId}/result` → `/api/recommendation/batch/result?batchId=...`

### 우선순위 2: 파라미터 이름 변경 (HIGH)

1. **Recommendation 배치**
   - `batchJobId` → `batchId`

2. **Report 조회**
   - `userId` → `reportId`

### 우선순위 3: 엔드포인트 경로 수정 (MEDIUM)

1. **Dashboard**
   - `/api/v1/dashboard/summary` → `/api/dashboard/summary`
   - `userId` query parameter 제거

---

## 📊 불일치 통계

- **총 엔드포인트 수**: 30개
- **불일치 엔드포인트**: 23개 (76.7%)
- **정상 작동**: 7개 (23.3%)

**심각도 분포**:
- 🔴 CRITICAL (404 에러): 19개
- 🟡 MEDIUM (422 에러): 4개
- ✅ 정상: 7개

---

## 📝 참고 사항

1. **FastAPI 측에서 이미 경로 변수를 제거함**
   - FastAPI OpenAPI 스펙에 "query parameters 사용" 명시
   - Spring Boot Controller도 이미 수정 완료 (이전 작업)
   - **FastApiClient만 업데이트 누락됨**

2. **422 Validation Error 발생 이유**
   - FastAPI가 `batchId`를 기대하는데 Spring Boot가 `batchJobId` 전송
   - FastAPI가 `reportId`를 기대하는데 Spring Boot가 `userId` 전송

3. **404 Not Found 발생 이유**
   - 경로 자체가 다름 (path variable vs query parameter)
   - FastAPI는 해당 경로를 라우팅하지 않음
