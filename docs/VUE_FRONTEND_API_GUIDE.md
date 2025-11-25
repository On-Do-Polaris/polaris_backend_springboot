# Vue Frontend ↔ Spring Boot API 연동 가이드

**작성일**: 2025-11-25
**버전**: v1.0
**Spring Boot 버전**: 3.x
**대상**: Vue.js 프론트엔드 팀

---

## 📋 목차

1. [시스템 개요](#시스템-개요)
2. [API 기본 정보](#api-기본-정보)
3. [인증 방식](#인증-방식)
4. [전체 API 목록](#전체-api-목록)
5. [API 상세 스펙](#api-상세-스펙)
6. [에러 처리](#에러-처리)
7. [Vue.js 연동 예제](#vuejs-연동-예제)
8. [TypeScript 타입 정의](#typescript-타입-정의)

---

## 시스템 개요

### 아키텍처

```
┌─────────────────┐         HTTP/REST API        ┌──────────────────┐
│   Vue.js        │ ───────────────────────────> │  Spring Boot     │
│   (Frontend)    │         JSON Request          │  (Backend API)   │
│                 │ <─────────────────────────────│                  │
└─────────────────┘         JSON Response        └──────────────────┘
                                                           │
                                                           ▼
                                                  ┌──────────────────┐
                                                  │   FastAPI        │
                                                  │   (AI Backend)   │
                                                  └──────────────────┘
```

### 주요 기능

- **사용자 인증**: JWT 기반 로그인/회원가입
- **사업장 관리**: CRUD 작업
- **AI 리스크 분석**: FastAPI AI Agent를 통한 물리적 리스크 분석
- **재무 영향 분석**: AAL (Average Annual Loss) 계산
- **시뮬레이션**: 사업장 이전 시뮬레이션, 기후 시뮬레이션
- **리포트 생성**: LLM 기반 TCFD/ESG 보고서

---

## API 기본 정보

### Base URL

```
Development: http://localhost:8080
Production:  http://{your-server-domain}
```

### 공통 헤더

```http
Content-Type: application/json
Authorization: Bearer {access_token}
```

### 응답 형식

모든 응답은 JSON 형식이며, 필드명은 **camelCase**를 사용합니다.

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "서울 본사",
  "status": "success"
}
```

### CORS 설정

기본적으로 다음 origin이 허용됩니다:
- `http://localhost:3000`
- `http://localhost:5173` (Vite 기본 포트)
- `http://localhost:8080`

---

## 인증 방식

### JWT 토큰 인증

모든 인증이 필요한 API 요청 시 **HTTP 헤더**에 JWT Access Token을 포함해야 합니다.

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 토큰 만료 시간

| 토큰 타입 | 만료 시간 |
|----------|---------|
| Access Token | 1시간 |
| Refresh Token | 30일 |

### 인증 예외 엔드포인트

다음 엔드포인트는 인증 없이 접근 가능합니다:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/health`

---

## 전체 API 목록

### 1. 인증 (Authentication) API

| 메서드 | 엔드포인트 | 설명 | 인증 필요 |
|--------|-----------|------|---------|
| POST | `/api/auth/register` | 회원가입 | ❌ |
| POST | `/api/auth/login` | 로그인 | ❌ |
| POST | `/api/auth/logout` | 로그아웃 | ✅ |
| POST | `/api/auth/refresh` | 토큰 갱신 | ❌ |

### 2. 사용자 (User) API

| 메서드 | 엔드포인트 | 설명 | 인증 필요 |
|--------|-----------|------|---------|
| GET | `/api/users/me` | 현재 사용자 정보 조회 | ✅ |
| PATCH | `/api/users/me` | 사용자 정보 수정 | ✅ |
| DELETE | `/api/users/me` | 사용자 삭제 (비활성화) | ✅ |

### 3. 사업장 (Site) API

| 메서드 | 엔드포인트 | 설명 | 인증 필요 |
|--------|-----------|------|---------|
| GET | `/api/sites` | 사업장 목록 조회 | ✅ |
| POST | `/api/sites` | 사업장 생성 | ✅ |
| PATCH | `/api/sites/{siteId}` | 사업장 수정 | ✅ |
| DELETE | `/api/sites/{siteId}` | 사업장 삭제 | ✅ |

### 4. 분석 (Analysis) API

| 메서드 | 엔드포인트 | 설명 | 인증 필요 |
|--------|-----------|------|---------|
| POST | `/api/sites/{siteId}/analysis/start` | AI 리스크 분석 시작 | ✅ |
| GET | `/api/sites/{siteId}/analysis/status/{jobId}` | 분석 작업 상태 조회 | ✅ |
| GET | `/api/sites/{siteId}/analysis/physical-risk-scores` | 물리적 리스크 점수 조회 | ✅ |
| GET | `/api/sites/{siteId}/analysis/past-events` | 과거 재난 이력 조회 | ✅ |
| GET | `/api/sites/{siteId}/analysis/financial-impacts` | 재무 영향(AAL) 조회 | ✅ |
| GET | `/api/sites/{siteId}/analysis/vulnerability` | 취약성 분석 조회 | ✅ |
| GET | `/api/sites/{siteId}/analysis/total` | 통합 분석 결과 조회 | ✅ |

### 5. 대시보드 (Dashboard) API

| 메서드 | 엔드포인트 | 설명 | 인증 필요 |
|--------|-----------|------|---------|
| GET | `/api/dashboard/summary` | 대시보드 요약 정보 | ✅ |

### 6. 시뮬레이션 (Simulation) API

| 메서드 | 엔드포인트 | 설명 | 인증 필요 |
|--------|-----------|------|---------|
| POST | `/api/simulation/relocation/compare` | 사업장 이전 시뮬레이션 | ✅ |
| POST | `/api/simulation/climate` | 기후 시뮬레이션 | ✅ |

### 7. 리포트 (Report) API

| 메서드 | 엔드포인트 | 설명 | 인증 필요 |
|--------|-----------|------|---------|
| POST | `/api/reports` | 리포트 생성 | ✅ |
| GET | `/api/reports/web` | 웹 리포트 뷰 조회 | ✅ |
| GET | `/api/reports/pdf` | PDF 리포트 조회 | ✅ |
| DELETE | `/api/reports` | 리포트 삭제 | ✅ |

### 8. 메타 데이터 (Meta) API

| 메서드 | 엔드포인트 | 설명 | 인증 필요 |
|--------|-----------|------|---------|
| GET | `/api/meta/hazards` | 지원하는 재해 유형 목록 | ✅ |
| GET | `/api/meta/industries` | 산업 분류 목록 | ✅ |
| GET | `/api/meta/ssp-scenarios` | SSP 시나리오 목록 | ✅ |

---

## API 상세 스펙

### 1. 회원가입

#### `POST /api/auth/register`

**Request**

```http
POST /api/auth/register HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "organizationName": "SKAX Corp",
  "name": "홍길동",
  "phone": "010-1234-5678"
}
```

**Response (201 Created)**

```json
{
  "userId": "user@example.com"
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| `userId` | string | 생성된 사용자 ID (이메일) |

---

### 2. 로그인

#### `POST /api/auth/login`

**Request**

```http
POST /api/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response (200 OK)**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "name": "홍길동",
    "organizationName": "SKAX Corp",
    "phone": "010-1234-5678",
    "role": "USER",
    "createdAt": "2025-11-25T10:30:00"
  }
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| `accessToken` | string | JWT Access Token (1시간 유효) |
| `refreshToken` | string | JWT Refresh Token (30일 유효) |
| `tokenType` | string | 토큰 타입 ("Bearer") |
| `expiresIn` | number | Access Token 만료 시간 (초) |
| `user` | object | 사용자 정보 |

---

### 3. 토큰 갱신

#### `POST /api/auth/refresh`

Access Token이 만료되었을 때 Refresh Token을 사용하여 새로운 토큰을 발급받습니다.

**Request**

```http
POST /api/auth/refresh HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK)**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": { ... }
}
```

---

### 4. 로그아웃

#### `POST /api/auth/logout`

**Request**

```http
POST /api/auth/logout HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK)**

```json
{
  "message": "로그아웃되었습니다"
}
```

---

### 5. 사업장 목록 조회

#### `GET /api/sites`

**Request**

```http
GET /api/sites HTTP/1.1
Host: localhost:8080
Authorization: Bearer {access_token}
```

**Response (200 OK)**

```json
{
  "sites": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "서울 본사",
      "address": "서울특별시 중구 세종대로 110",
      "latitude": 37.5665,
      "longitude": 126.9780,
      "industryType": "제조업",
      "riskScore": 75,
      "createdAt": "2025-11-01T10:00:00",
      "updatedAt": "2025-11-25T14:30:00"
    }
  ]
}
```

---

### 6. 사업장 생성

#### `POST /api/sites`

**Request**

```http
POST /api/sites HTTP/1.1
Host: localhost:8080
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "name": "부산 공장",
  "roadAddress": "부산광역시 해운대구 센텀중앙로 78",
  "jibunAddress": "부산광역시 해운대구 우동 1511",
  "latitude": 35.1696,
  "longitude": 129.1305,
  "industryType": "제조업",
  "buildingType": "공장",
  "buildingAge": 10,
  "floorArea": 5000.0,
  "assetValue": 10000000000
}
```

**Response (201 Created)**

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "name": "부산 공장",
  "address": "부산광역시 해운대구 센텀중앙로 78",
  "latitude": 35.1696,
  "longitude": 129.1305,
  "industryType": "제조업",
  "riskScore": null,
  "createdAt": "2025-11-25T15:00:00",
  "updatedAt": "2025-11-25T15:00:00"
}
```

---

### 7. AI 리스크 분석 시작

#### `POST /api/sites/{siteId}/analysis/start`

AI Agent를 사용하여 사업장의 기후 물리적 리스크를 분석합니다.

**Request**

```http
POST /api/sites/550e8400-e29b-41d4-a716-446655440000/analysis/start HTTP/1.1
Host: localhost:8080
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "latitude": 37.5665,
  "longitude": 126.9780,
  "industryType": "제조업"
}
```

**Response (200 OK)**

```json
{
  "jobId": "123e4567-e89b-12d3-a456-426614174000",
  "siteId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "running",
  "progress": 10,
  "currentNode": "data_collection",
  "currentHazard": null,
  "startedAt": "2025-11-25T15:30:00",
  "completedAt": null,
  "estimatedCompletionTime": "2025-11-25T15:35:00",
  "error": null
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| `jobId` | UUID | 작업 ID (상태 조회 시 사용) |
| `status` | string | `queued`, `running`, `completed`, `failed` |
| `progress` | integer | 진행률 (0-100) |
| `currentNode` | string | 현재 실행 중인 워크플로우 노드 |
| `estimatedCompletionTime` | datetime | 예상 완료 시간 (ISO 8601) |

---

### 8. 분석 작업 상태 조회

#### `GET /api/sites/{siteId}/analysis/status/{jobId}`

진행 중인 분석 작업의 상태를 폴링하여 조회합니다.

**Request**

```http
GET /api/sites/550e8400-e29b-41d4-a716-446655440000/analysis/status/123e4567-e89b-12d3-a456-426614174000 HTTP/1.1
Host: localhost:8080
Authorization: Bearer {access_token}
```

**Response (200 OK) - 진행 중**

```json
{
  "jobId": "123e4567-e89b-12d3-a456-426614174000",
  "siteId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "running",
  "progress": 45,
  "currentNode": "aal_analysis",
  "currentHazard": "HIGH_TEMPERATURE",
  "startedAt": "2025-11-25T15:30:00",
  "completedAt": null,
  "estimatedCompletionTime": "2025-11-25T15:35:00",
  "error": null
}
```

**Response (200 OK) - 완료**

```json
{
  "jobId": "123e4567-e89b-12d3-a456-426614174000",
  "siteId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "completed",
  "progress": 100,
  "currentNode": "completed",
  "currentHazard": null,
  "startedAt": "2025-11-25T15:30:00",
  "completedAt": "2025-11-25T15:35:23",
  "estimatedCompletionTime": null,
  "error": null
}
```

---

### 9. 물리적 리스크 점수 조회

#### `GET /api/sites/{siteId}/analysis/physical-risk-scores?hazardType={type}`

SSP 시나리오별 물리적 리스크 점수를 조회합니다.

**Request**

```http
GET /api/sites/550e8400-e29b-41d4-a716-446655440000/analysis/physical-risk-scores?hazardType=HIGH_TEMPERATURE HTTP/1.1
Host: localhost:8080
Authorization: Bearer {access_token}
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `hazardType` | string | Optional | 특정 재해 유형 필터 (없으면 전체) |

**Response (200 OK)**

```json
{
  "scenarios": [
    {
      "scenario": "SSP2-4.5",
      "riskType": "HIGH_TEMPERATURE",
      "shortTerm": {
        "q1": 65,
        "q2": 72,
        "q3": 78,
        "q4": 70
      },
      "midTerm": {
        "year2026": 68,
        "year2027": 70,
        "year2028": 73,
        "year2029": 75,
        "year2030": 77
      },
      "longTerm": {
        "year2020s": 72,
        "year2030s": 78,
        "year2040s": 84,
        "year2050s": 89
      }
    }
  ]
}
```

---

### 10. 재무 영향 (AAL) 조회

#### `GET /api/sites/{siteId}/analysis/financial-impacts`

SSP 시나리오별 AAL (Average Annual Loss) 분석 결과를 조회합니다.

**Request**

```http
GET /api/sites/550e8400-e29b-41d4-a716-446655440000/analysis/financial-impacts HTTP/1.1
Host: localhost:8080
Authorization: Bearer {access_token}
```

**Response (200 OK)**

```json
{
  "scenarios": [
    {
      "scenario": "SSP2-4.5",
      "riskType": "HIGH_TEMPERATURE",
      "shortTerm": {
        "q1": 0.015,
        "q2": 0.018,
        "q3": 0.021,
        "q4": 0.019
      },
      "midTerm": {
        "year2026": 0.023,
        "year2027": 0.025,
        "year2028": 0.027,
        "year2029": 0.029,
        "year2030": 0.031
      },
      "longTerm": {
        "year2020s": 0.028,
        "year2030s": 0.035,
        "year2040s": 0.042,
        "year2050s": 0.051
      }
    }
  ]
}
```

**AAL 값 해석**
- AAL 값은 `0.0 ~ 1.0` 범위의 비율입니다
- 예: `0.015` = 1.5% = 자산 가치의 1.5%가 연평균 손실
- 자산 가치가 500억원이면: `500억 × 0.015 = 7.5억원` 연평균 손실

---

### 11. 사업장 이전 시뮬레이션

#### `POST /api/simulation/relocation/compare`

현재 위치와 후보 위치의 리스크를 비교합니다.

**Request**

```http
POST /api/simulation/relocation/compare HTTP/1.1
Host: localhost:8080
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "currentSiteId": "550e8400-e29b-41d4-a716-446655440000",
  "latitude": 35.1796,
  "longitude": 129.0756,
  "roadAddress": "부산광역시 해운대구 센텀중앙로 78",
  "jibunAddress": "부산광역시 해운대구 우동 1511"
}
```

**Response (200 OK)**

```json
{
  "currentLocation": {
    "risks": [
      {
        "riskType": "폭염",
        "riskScore": 75,
        "aal": 0.023
      },
      {
        "riskType": "태풍",
        "riskScore": 70,
        "aal": 0.018
      }
    ]
  },
  "newLocation": {
    "risks": [
      {
        "riskType": "폭염",
        "riskScore": 65,
        "aal": 0.018
      },
      {
        "riskType": "태풍",
        "riskScore": 85,
        "aal": 0.032
      }
    ]
  }
}
```

---

### 12. 대시보드 요약 정보

#### `GET /api/dashboard/summary`

전체 사업장의 요약 정보를 조회합니다.

**Request**

```http
GET /api/dashboard/summary HTTP/1.1
Host: localhost:8080
Authorization: Bearer {access_token}
```

**Response (200 OK)**

```json
{
  "mainClimateRisk": "극심한 고온",
  "sites": [
    {
      "siteId": "550e8400-e29b-41d4-a716-446655440000",
      "siteName": "서울 본사",
      "siteType": "공장",
      "location": "서울특별시 강남구",
      "totalRiskScore": 75
    }
  ]
}
```

---

## 에러 처리

### HTTP 상태 코드

| 상태 코드 | 설명 | 대응 방법 |
|----------|------|----------|
| `200 OK` | 성공 | - |
| `201 Created` | 리소스 생성 성공 | - |
| `400 Bad Request` | 잘못된 요청 | Request Body 검증 |
| `401 Unauthorized` | 인증 실패 | 로그인 필요 또는 토큰 갱신 |
| `403 Forbidden` | 권한 없음 | 접근 권한 확인 |
| `404 Not Found` | 리소스 없음 | ID 확인 |
| `409 Conflict` | 중복 리소스 | 이미 존재하는 데이터 |
| `422 Unprocessable Entity` | 유효성 검증 실패 | 필드 형식 확인 |
| `500 Internal Server Error` | 서버 오류 | 관리자에게 문의 |

### 에러 응답 형식

**유효성 검증 실패 (400)**

```json
{
  "timestamp": "2025-11-25T15:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/sites"
}
```

**인증 실패 (401)**

```json
{
  "timestamp": "2025-11-25T15:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "인증이 필요합니다",
  "path": "/api/sites"
}
```

**리소스 없음 (404)**

```json
{
  "timestamp": "2025-11-25T15:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "사업장을 찾을 수 없습니다",
  "path": "/api/sites/550e8400-e29b-41d4-a716-446655440000"
}
```

---

## Vue.js 연동 예제

### 1. Axios 설정

#### `src/api/axios.ts`

```typescript
import axios, { AxiosInstance, AxiosError } from 'axios';
import router from '@/router';

// Axios 인스턴스 생성
const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: 자동으로 Access Token 추가
apiClient.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: 토큰 갱신 및 에러 처리
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config;

    // 401 Unauthorized: Access Token 만료
    if (error.response?.status === 401 && originalRequest) {
      try {
        const refreshToken = localStorage.getItem('refreshToken');

        if (!refreshToken) {
          throw new Error('No refresh token');
        }

        // 토큰 갱신 요청
        const { data } = await axios.post(
          'http://localhost:8080/api/auth/refresh',
          { refreshToken }
        );

        // 새로운 토큰 저장
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);

        // 원래 요청 재시도
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        // 토큰 갱신 실패: 로그아웃 처리
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        router.push('/login');
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
```

---

### 2. API 서비스 예제

#### `src/api/authService.ts`

```typescript
import apiClient from './axios';
import { LoginRequest, LoginResponse, RegisterRequest } from '@/types/auth';

export const authService = {
  // 회원가입
  async register(data: RegisterRequest): Promise<{ userId: string }> {
    const response = await apiClient.post('/api/auth/register', data);
    return response.data;
  },

  // 로그인
  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await apiClient.post('/api/auth/login', data);

    // 토큰 저장
    localStorage.setItem('accessToken', response.data.accessToken);
    localStorage.setItem('refreshToken', response.data.refreshToken);

    return response.data;
  },

  // 로그아웃
  async logout(): Promise<void> {
    try {
      await apiClient.post('/api/auth/logout');
    } finally {
      // 토큰 삭제
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    }
  },

  // 현재 사용자 정보 조회
  async getCurrentUser() {
    const response = await apiClient.get('/api/users/me');
    return response.data;
  },
};
```

#### `src/api/siteService.ts`

```typescript
import apiClient from './axios';
import { Site, CreateSiteRequest, UpdateSiteRequest } from '@/types/site';

export const siteService = {
  // 사업장 목록 조회
  async getSites(): Promise<{ sites: Site[] }> {
    const response = await apiClient.get('/api/sites');
    return response.data;
  },

  // 사업장 생성
  async createSite(data: CreateSiteRequest): Promise<Site> {
    const response = await apiClient.post('/api/sites', data);
    return response.data;
  },

  // 사업장 수정
  async updateSite(siteId: string, data: UpdateSiteRequest): Promise<Site> {
    const response = await apiClient.patch(`/api/sites/${siteId}`, data);
    return response.data;
  },

  // 사업장 삭제
  async deleteSite(siteId: string): Promise<void> {
    await apiClient.delete(`/api/sites/${siteId}`);
  },
};
```

#### `src/api/analysisService.ts`

```typescript
import apiClient from './axios';
import {
  StartAnalysisRequest,
  AnalysisJobStatus,
  PhysicalRiskScoreResponse,
  FinancialImpactResponse,
} from '@/types/analysis';

export const analysisService = {
  // 분석 시작
  async startAnalysis(
    siteId: string,
    data: StartAnalysisRequest
  ): Promise<AnalysisJobStatus> {
    const response = await apiClient.post(
      `/api/sites/${siteId}/analysis/start`,
      data
    );
    return response.data;
  },

  // 분석 상태 조회
  async getAnalysisStatus(
    siteId: string,
    jobId: string
  ): Promise<AnalysisJobStatus> {
    const response = await apiClient.get(
      `/api/sites/${siteId}/analysis/status/${jobId}`
    );
    return response.data;
  },

  // 분석 완료 대기 (폴링)
  async waitForCompletion(
    siteId: string,
    jobId: string,
    pollInterval: number = 5000
  ): Promise<AnalysisJobStatus> {
    return new Promise((resolve, reject) => {
      const poll = async () => {
        try {
          const status = await this.getAnalysisStatus(siteId, jobId);

          if (status.status === 'completed') {
            resolve(status);
          } else if (status.status === 'failed') {
            reject(new Error('Analysis failed'));
          } else {
            // 계속 폴링
            setTimeout(poll, pollInterval);
          }
        } catch (error) {
          reject(error);
        }
      };

      poll();
    });
  },

  // 물리적 리스크 점수 조회
  async getPhysicalRiskScores(
    siteId: string,
    hazardType?: string
  ): Promise<PhysicalRiskScoreResponse> {
    const params = hazardType ? { hazardType } : {};
    const response = await apiClient.get(
      `/api/sites/${siteId}/analysis/physical-risk-scores`,
      { params }
    );
    return response.data;
  },

  // 재무 영향 조회
  async getFinancialImpact(siteId: string): Promise<FinancialImpactResponse> {
    const response = await apiClient.get(
      `/api/sites/${siteId}/analysis/financial-impacts`
    );
    return response.data;
  },
};
```

---

### 3. Vue 컴포넌트 사용 예제

#### `src/views/AnalysisView.vue`

```vue
<template>
  <div class="analysis-view">
    <h1>AI 리스크 분석</h1>

    <!-- 분석 시작 버튼 -->
    <button @click="startAnalysis" :disabled="isAnalyzing">
      {{ isAnalyzing ? '분석 중...' : '분석 시작' }}
    </button>

    <!-- 진행률 표시 -->
    <div v-if="isAnalyzing" class="progress-bar">
      <div class="progress" :style="{ width: `${progress}%` }"></div>
      <p>{{ progress }}% - {{ currentNode }}</p>
    </div>

    <!-- 결과 표시 -->
    <div v-if="riskScores" class="results">
      <h2>물리적 리스크 점수</h2>
      <div v-for="scenario in riskScores.scenarios" :key="scenario.scenario">
        <h3>{{ scenario.scenario }}</h3>
        <p>리스크 타입: {{ scenario.riskType }}</p>
        <p>단기 Q1: {{ scenario.shortTerm.q1 }}점</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { analysisService } from '@/api/analysisService';
import type { AnalysisJobStatus, PhysicalRiskScoreResponse } from '@/types/analysis';

const siteId = ref('550e8400-e29b-41d4-a716-446655440000');
const isAnalyzing = ref(false);
const progress = ref(0);
const currentNode = ref('');
const riskScores = ref<PhysicalRiskScoreResponse | null>(null);

// 분석 시작
const startAnalysis = async () => {
  try {
    isAnalyzing.value = true;
    progress.value = 0;

    // 1. 분석 시작
    const startResponse = await analysisService.startAnalysis(siteId.value, {
      latitude: 37.5665,
      longitude: 126.9780,
      industryType: '제조업',
    });

    console.log('분석 시작:', startResponse);
    const jobId = startResponse.jobId;

    // 2. 폴링으로 진행 상황 업데이트
    const pollInterval = setInterval(async () => {
      const status = await analysisService.getAnalysisStatus(siteId.value, jobId);

      progress.value = status.progress;
      currentNode.value = status.currentNode || '';

      if (status.status === 'completed') {
        clearInterval(pollInterval);
        await loadResults();
        isAnalyzing.value = false;
      } else if (status.status === 'failed') {
        clearInterval(pollInterval);
        alert('분석 실패');
        isAnalyzing.value = false;
      }
    }, 3000); // 3초마다 폴링

  } catch (error) {
    console.error('분석 시작 실패:', error);
    alert('분석 시작에 실패했습니다');
    isAnalyzing.value = false;
  }
};

// 결과 로드
const loadResults = async () => {
  try {
    riskScores.value = await analysisService.getPhysicalRiskScores(siteId.value);
    console.log('리스크 점수:', riskScores.value);
  } catch (error) {
    console.error('결과 로드 실패:', error);
  }
};
</script>

<style scoped>
.progress-bar {
  width: 100%;
  height: 30px;
  background-color: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
  margin: 20px 0;
}

.progress {
  height: 100%;
  background-color: #4caf50;
  transition: width 0.3s ease;
}

.results {
  margin-top: 30px;
}
</style>
```

---

## TypeScript 타입 정의

### `src/types/auth.ts`

```typescript
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  organizationName: string;
  name: string;
  phone: string;
}

export interface User {
  id: string;
  email: string;
  name: string;
  organizationName: string;
  phone: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}
```

### `src/types/site.ts`

```typescript
export interface Site {
  id: string;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  industryType: string;
  riskScore: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSiteRequest {
  name: string;
  roadAddress: string;
  jibunAddress: string;
  latitude: number;
  longitude: number;
  industryType: string;
  buildingType: string;
  buildingAge: number;
  floorArea: number;
  assetValue: number;
}

export interface UpdateSiteRequest {
  name?: string;
  roadAddress?: string;
  jibunAddress?: string;
  buildingAge?: number;
  floorArea?: number;
  assetValue?: number;
}
```

### `src/types/analysis.ts`

```typescript
export interface StartAnalysisRequest {
  latitude: number;
  longitude: number;
  industryType: string;
}

export interface AnalysisJobStatus {
  jobId: string;
  siteId: string;
  status: 'queued' | 'running' | 'completed' | 'failed';
  progress: number;
  currentNode: string | null;
  currentHazard: string | null;
  startedAt: string;
  completedAt: string | null;
  estimatedCompletionTime: string | null;
  error: any | null;
}

export interface PhysicalRiskScoreResponse {
  scenarios: Array<{
    scenario: string;
    riskType: string;
    shortTerm: {
      q1: number;
      q2: number;
      q3: number;
      q4: number;
    };
    midTerm: {
      year2026: number;
      year2027: number;
      year2028: number;
      year2029: number;
      year2030: number;
    };
    longTerm: {
      year2020s: number;
      year2030s: number;
      year2040s: number;
      year2050s: number;
    };
  }>;
}

export interface FinancialImpactResponse {
  scenarios: Array<{
    scenario: string;
    riskType: string;
    shortTerm: {
      q1: number;
      q2: number;
      q3: number;
      q4: number;
    };
    midTerm: {
      year2026: number;
      year2027: number;
      year2028: number;
      year2029: number;
      year2030: number;
    };
    longTerm: {
      year2020s: number;
      year2030s: number;
      year2040s: number;
      year2050s: number;
    };
  }>;
}
```

---

## 환경 변수 설정

### `.env.development`

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=SKAX Physical Risk Management
```

### `.env.production`

```bash
VITE_API_BASE_URL=https://your-production-server.com
VITE_APP_NAME=SKAX Physical Risk Management
```

---

## 주의사항

### 1. CORS 설정

개발 환경에서 CORS 문제가 발생할 경우:
- Spring Boot 서버의 `application.yml`에서 `cors.allowed-origins`에 Vue 개발 서버 주소 추가
- 기본값: `http://localhost:5173` (Vite)

### 2. 토큰 보안

- Access Token과 Refresh Token을 `localStorage`에 저장
- XSS 공격 방지를 위해 프로덕션에서는 `httpOnly` 쿠키 사용 고려
- HTTPS 사용 필수

### 3. 폴링 최적화

- 분석 작업 상태 조회 시 3~5초 간격으로 폴링
- 완료되면 폴링 중단
- 너무 짧은 간격은 서버 부하 증가

### 4. 에러 처리

- 모든 API 호출에 try-catch 사용
- 사용자 친화적인 에러 메시지 표시
- 401 에러 시 자동 토큰 갱신 또는 로그인 페이지 리다이렉트

---

## 추가 리소스

- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (개발 환경)
- **Spring Boot 소스코드**: `backend_team_java/src/main/java/com/skax/physicalrisk/controller/`
- **API 변경 이력**: [FASTAPI_URL_FIX_REPORT.md](./FASTAPI_URL_FIX_REPORT.md)
- **AAL v11 업데이트**: [AAL_V11_API_IMPACT_ANALYSIS.md](./AAL_V11_API_IMPACT_ANALYSIS.md)

---

**작성자**: Backend Team
**최종 업데이트**: 2025-11-25
**버전**: 1.0
**문서 상태**: ✅ 리뷰 완료
**대상 독자**: Vue.js 프론트엔드 개발자
