# SKAX Physical Risk Management API

사업장 기후 물리적 리스크 관리 시스템 백엔드 API

## 개요

SKAX Physical Risk Management는 기업의 사업장에 대한 기후 변화 관련 물리적 리스크를 분석하고 관리하는 시스템입니다. Spring Boot를 기반으로 개발되었으며, FastAPI AI 서버와 연동하여 AI 기반 리스크 분석 기능을 제공합니다.

## 기술 스택

- **Java**: 21
- **Spring Boot**: 3.5.7
- **Database**: PostgreSQL (운영), H2 (로컬 테스트)
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA / Hibernate
- **Cache**: Caffeine
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven
- **Testing**: JUnit 5, JaCoCo (코드 커버리지 60% 이상)

## 주요 기능

### 1. 인증/인가
- JWT 기반 인증 (Access Token + Refresh Token)
- 회원가입, 로그인, 비밀번호 재설정
- 이메일 인증 (SendGrid)

### 2. 사업장 관리
- 사업장 등록, 수정, 삭제, 조회
- 사업장별 산업 분류 및 위치 정보 관리

### 3. 리스크 분석
- AI 기반 기후 물리적 리스크 분석
- 비동기 분석 작업 처리 (폴링 방식)
- 재해 유형별 취약성 평가
- 과거 재해 이력 분석
- 재무 영향 평가

### 4. 시뮬레이션
- 기후 시나리오 기반 시뮬레이션
- 사업장 이전 시뮬레이션

### 5. 리포트
- 분석 결과 리포트 생성 (PDF, Web View)
- 리포트 조회 및 다운로드

### 6. 대시보드
- 사용자별 사업장 요약 정보
- 리스크 현황 시각화

## 프로젝트 구조

```
src/main/java/com/skax/physicalrisk/
├── client/              # 외부 API 클라이언트
│   └── fastapi/        # FastAPI 클라이언트
├── config/             # 설정 파일
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   ├── CacheConfig.java
│   └── AsyncConfig.java
├── constants/          # 상수 정의
├── controller/         # REST API 컨트롤러
│   ├── AuthController.java
│   ├── UserController.java
│   ├── SiteController.java
│   ├── AnalysisController.java
│   ├── SimulationController.java
│   ├── ReportController.java
│   ├── DashboardController.java
│   └── MetaController.java
├── domain/             # 도메인 모델
│   ├── user/          # 사용자 도메인
│   ├── site/          # 사업장 도메인
│   ├── analysis/      # 분석 도메인
│   ├── report/        # 리포트 도메인
│   └── meta/          # 메타데이터 도메인
├── dto/               # 데이터 전송 객체
│   ├── request/
│   └── response/
├── exception/         # 예외 처리
├── security/          # 보안 관련
│   ├── jwt/          # JWT 인증
│   └── filter/       # 필터
├── service/           # 비즈니스 로직
└── util/              # 유틸리티
```

## 시작하기

### 사전 요구사항

- Java 21 이상
- Maven 3.6 이상
- PostgreSQL 12 이상
- FastAPI AI 서버 (선택사항)

### 설치 및 실행

1. **프로젝트 클론**
```bash
git clone <repository-url>
cd backend_team_java
```

2. **환경 변수 설정**

`.env.example` 파일을 `.env`로 복사하고 필요한 값을 설정합니다.

```bash
cp .env.example .env
```

필수 환경 변수:
- `DB_USERNAME`, `DB_PASSWORD`: 데이터베이스 접속 정보
- `JWT_SECRET`: JWT 시크릿 키 (최소 256비트)
- `FASTAPI_BASE_URL`, `FASTAPI_API_KEY`: FastAPI 서버 정보
- `MAIL_USERNAME`, `MAIL_PASSWORD`: SendGrid 메일 설정
- `KAKAO_API_KEY`: 카카오 API 키 (주소 검색용)

3. **데이터베이스 설정**

PostgreSQL에 데이터베이스를 생성합니다.

```sql
CREATE DATABASE skala_application;
CREATE USER skala_app_user WITH PASSWORD 'your-password';
GRANT ALL PRIVILEGES ON DATABASE skala_application TO skala_app_user;
```

4. **빌드**

```bash
mvn clean install
```

5. **실행**

```bash
mvn spring-boot:run
```

또는 JAR 파일로 실행:

```bash
java -jar target/physical-risk-management-1.0.0.jar
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

### 로컬 개발 (H2 데이터베이스)

로컬 개발 시 H2 인메모리 데이터베이스를 사용할 수 있습니다.

`src/main/resources/application-local.yml` 파일을 생성하거나 프로파일을 `local`로 설정:

```yaml
spring:
  profiles:
    active: local
```

## API 문서

애플리케이션 실행 후 Swagger UI를 통해 API 문서를 확인할 수 있습니다.

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## 주요 API 엔드포인트

### 인증
- `POST /api/auth/register` - 회원가입
- `POST /api/auth/login` - 로그인
- `POST /api/auth/refresh` - Access Token 갱신
- `POST /api/auth/password-reset` - 비밀번호 재설정 요청
- `POST /api/auth/password-reset/confirm` - 비밀번호 재설정 확인

### 사업장
- `GET /api/sites` - 사업장 목록 조회
- `POST /api/sites` - 사업장 등록
- `GET /api/sites/{id}` - 사업장 상세 조회
- `PUT /api/sites/{id}` - 사업장 수정
- `DELETE /api/sites/{id}` - 사업장 삭제

### 분석
- `POST /api/analysis/start` - 리스크 분석 시작
- `GET /api/analysis/job/{jobId}/status` - 분석 작업 상태 조회
- `GET /api/analysis/sites/{siteId}` - 사업장 분석 결과 조회

### 시뮬레이션
- `POST /api/simulation/climate` - 기후 시뮬레이션
- `POST /api/simulation/relocation` - 이전 시뮬레이션

### 리포트
- `POST /api/report` - 리포트 생성
- `GET /api/report/{reportId}/web` - 웹 리포트 조회
- `GET /api/report/{reportId}/pdf` - PDF 리포트 다운로드

### 대시보드
- `GET /api/dashboard/summary` - 대시보드 요약 정보

## 테스트

```bash
# 전체 테스트 실행
mvn test

# 코드 커버리지 리포트 생성
mvn clean test jacoco:report
```

코드 커버리지 리포트는 `target/site/jacoco/index.html`에서 확인할 수 있습니다.

## 배포

### Docker

Docker를 사용한 배포는 GitHub Actions CI/CD 워크플로우를 통해 자동화되어 있습니다.

```bash
# Docker 이미지 빌드
docker build -t physical-risk-api .

# Docker 컨테이너 실행
docker run -p 8080:8080 --env-file .env physical-risk-api
```

### CI/CD

- **CI**: GitHub Actions를 통해 빌드 및 테스트 자동화
- **CD**: GCP Artifact Registry에 Docker 이미지 푸시 후 서버에 자동 배포

## 환경별 프로파일

- `local`: 로컬 개발 환경 (H2 데이터베이스)
- `staging`: 스테이징 환경
- `prod`: 운영 환경

프로파일은 `SPRING_PROFILES_ACTIVE` 환경 변수로 설정합니다.

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

## 보안

- 모든 API는 HTTPS를 통해 전송되어야 합니다 (운영 환경)
- JWT 토큰은 HTTP-only 쿠키에 저장하는 것을 권장합니다
- 환경 변수 파일(`.env`)은 절대 Git에 커밋하지 마세요
- JWT Secret은 최소 256비트 이상이어야 합니다

## 트러블슈팅

### 데이터베이스 연결 오류

- PostgreSQL이 실행 중인지 확인
- `.env` 파일의 DB 접속 정보가 정확한지 확인
- 방화벽 설정 확인

### JWT 토큰 오류

- JWT_SECRET 환경 변수가 설정되어 있는지 확인
- 토큰 만료 시간 확인 (Access Token: 1시간, Refresh Token: 30일)

### FastAPI 연결 오류

- FastAPI 서버가 실행 중인지 확인
- `FASTAPI_BASE_URL`이 올바른지 확인
- FastAPI API Key가 유효한지 확인

## 라이선스

이 프로젝트는 SKAX의 소유입니다.

## 기여

이 프로젝트는 SKAX 내부 프로젝트입니다. 기여 가이드라인은 팀 내부 문서를 참조하세요.

## 연락처

문의사항이 있으시면 개발팀에 문의해주세요.
