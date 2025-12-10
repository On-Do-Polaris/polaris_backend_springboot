# 경로 변수(Path Variable)가 포함된 엔드포인트 목록

**작성일**: 2025-12-09
**목적**: Spring Boot API에서 `{siteId}`, `{jobId}` 등 경로 변수를 사용하는 엔드포인트 정리

---

## 1. 사업장(Site) 관련

### 기본 경로: `/api/sites`

| HTTP Method | 엔드포인트 | 경로 변수 | 설명 |
|-------------|-----------|----------|------|
| `PATCH` | `/api/sites/{siteId}` | `siteId` (UUID) | 사업장 정보 수정 |
| `DELETE` | `/api/sites/{siteId}` | `siteId` (UUID) | 사업장 삭제 |

---

## 2. 분석(Analysis) 관련

### 기본 경로: `/api/sites/{siteId}/analysis`

| HTTP Method | 엔드포인트 | 경로 변수 | 설명 |
|-------------|-----------|----------|------|
| `POST` | `/api/sites/{siteId}/analysis/start` | `siteId` (UUID) | 분석 시작 |
| `GET` | `/api/sites/{siteId}/analysis/status/{jobId}` | `siteId` (UUID), `jobId` (UUID) | 분석 작업 상태 조회 |
| `GET` | `/api/sites/{siteId}/analysis/physical-risk-scores` | `siteId` (UUID) | 물리적 리스크 점수 조회 |
| `GET` | `/api/sites/{siteId}/analysis/past-events` | `siteId` (UUID) | 과거 재난 이력 조회 |
| `GET` | `/api/sites/{siteId}/analysis/ssp` | `siteId` (UUID) | SSP 시나리오 전망 조회 |
| `GET` | `/api/sites/{siteId}/analysis/financial-impacts` | `siteId` (UUID) | 재무 영향 분석 조회 |
| `GET` | `/api/sites/{siteId}/analysis/vulnerability` | `siteId` (UUID) | 취약성 분석 조회 |
| `GET` | `/api/sites/{siteId}/analysis/total` | `siteId` (UUID) | 통합 분석 결과 조회 |

---

## 3. 추가 데이터(Additional Data) 관련

### 기본 경로: `/api/sites/{siteId}/additional-data`

| HTTP Method | 엔드포인트 | 경로 변수 | 설명 |
|-------------|-----------|----------|------|
| `POST` | `/api/sites/{siteId}/additional-data` | `siteId` (UUID) | 추가 데이터 업로드 |
| `GET` | `/api/sites/{siteId}/additional-data` | `siteId` (UUID) | 추가 데이터 조회 (쿼리: `dataCategory`) |
| `DELETE` | `/api/sites/{siteId}/additional-data/{dataId}` | `siteId` (UUID), `dataId` (UUID) | 추가 데이터 삭제 |
| `GET` | `/api/sites/{siteId}/additional-data/{dataId}/structured` | `siteId` (UUID), `dataId` (UUID) | 정형화된 데이터 조회 |

---

## 4. 후보지 추천 배치(Recommendation Batch) 관련

### 기본 경로: `/api/recommendation`

| HTTP Method | 엔드포인트 | 경로 변수 | 설명 |
|-------------|-----------|----------|------|
| `GET` | `/api/recommendation/{batchJobId}/progress` | `batchJobId` (UUID) | 배치 작업 진행 상황 조회 |
| `GET` | `/api/recommendation/{batchJobId}/result` | `batchJobId` (UUID) | 배치 작업 결과 조회 |

---

## 5. 리포트(Report) 관련

### 기본 경로: `/api/reports`

**참고**: 리포트 API는 경로 변수 대신 **쿼리 파라미터**로 `userId`를 사용합니다.

| HTTP Method | 엔드포인트 | 쿼리 파라미터 | 설명 |
|-------------|-----------|--------------|------|
| `GET` | `/api/reports/web?userId={userId}` | `userId` (UUID) | 리포트 웹 뷰 조회 |
| `GET` | `/api/reports/pdf?userId={userId}` | `userId` (UUID) | 리포트 PDF 다운로드 정보 |
| `DELETE` | `/api/reports?userId={userId}` | `userId` (UUID) | 리포트 삭제 |

---

## 경로 변수 없는 주요 엔드포인트

참고로, 다음 엔드포인트들은 경로 변수를 사용하지 않습니다:

### 인증(Auth)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/auth/refresh`
- `POST /api/auth/password/reset-request`
- `POST /api/auth/password/reset-confirm`

### 사용자(User)
- `GET /api/users/me`
- `PATCH /api/users/me`
- `DELETE /api/users/me`
- `PATCH /api/users/me/password`

### 사업장(Site)
- `GET /api/sites` (전체 목록)
- `POST /api/sites` (생성)

### 대시보드(Dashboard)
- `GET /api/dashboard/summary`

### 메타 정보(Meta)
- `GET /api/meta/hazard-types`
- `GET /api/meta/ssp-scenarios`
- `GET /api/meta/industries`

### 시뮬레이션(Simulation)
- `POST /api/simulation/relocation/compare`
- `POST /api/simulation/climate`

### 재난 이력(Disaster History)
- `GET /api/disaster-history` (쿼리 파라미터 사용)

### 후보지 추천(Recommendation)
- `POST /api/recommendation` (배치 시작)

### 리포트(Report)
- `POST /api/reports` (생성)

### 헬스 체크(Health)
- `GET /api/health`

---

## 경로 변수 타입

모든 경로 변수는 **UUID** 타입입니다:

- `{siteId}`: 사업장 고유 ID
- `{jobId}`: 분석 작업 고유 ID
- `{dataId}`: 추가 데이터 고유 ID
- `{batchJobId}`: 배치 작업 고유 ID

**UUID 형식 예시**:
```
550e8400-e29b-41d4-a716-446655440000
```

---

## 사용 예시

### 1. 사업장 수정
```http
PATCH /api/sites/0108c964-fd03-4dfd-82e3-caf5674f62bd
Content-Type: application/json

{
  "name": "SK U 타워",
  "roadAddress": "경기도 성남시 분당구 성남대로343번길 9"
}
```

### 2. 분석 작업 상태 조회
```http
GET /api/sites/0108c964-fd03-4dfd-82e3-caf5674f62bd/analysis/status/abc12345-def6-7890-ghij-klmnopqrstuv
Authorization: Bearer {token}
```

### 3. 추가 데이터 삭제
```http
DELETE /api/sites/0108c964-fd03-4dfd-82e3-caf5674f62bd/additional-data/data1234-5678-90ab-cdef-ghijklmnopqr
Authorization: Bearer {token}
```

### 4. 배치 작업 진행 상황 조회
```http
GET /api/recommendation/batch123-4567-89ab-cdef-0123456789ab/progress
Authorization: Bearer {token}
```

---

## 정리

### 총 경로 변수 사용 엔드포인트 수: **14개**

#### 변수 1개 (12개)
- 사업장: 2개 (`PATCH`, `DELETE`)
- 분석: 7개 (`POST start`, `GET physical-risk-scores`, `GET past-events`, `GET ssp`, `GET financial-impacts`, `GET vulnerability`, `GET total`)
- 추가 데이터: 2개 (`POST`, `GET`)
- 배치: 2개 (`GET progress`, `GET result`)

#### 변수 2개 (2개)
- 분석: 1개 (`GET status/{jobId}`)
- 추가 데이터: 2개 (`DELETE /{dataId}`, `GET /{dataId}/structured`)

---

## 주의 사항

1. **인증 필수**: 대부분의 엔드포인트는 JWT 토큰이 필요합니다 (`Authorization: Bearer {token}`)
2. **권한 확인**: `{siteId}`를 사용하는 엔드포인트는 해당 사업장이 현재 사용자 소유인지 자동으로 검증합니다
3. **UUID 형식**: 잘못된 UUID 형식을 전송하면 `400 Bad Request` 오류가 발생합니다
4. **존재하지 않는 리소스**: 존재하지 않는 ID를 사용하면 `404 Not Found` 오류가 발생합니다

---

## 변경 이력

### 2025-12-09
- 초기 문서 작성
- 전체 경로 변수 엔드포인트 정리
