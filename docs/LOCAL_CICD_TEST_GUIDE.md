# 🚀 로컬 CI/CD 테스트 가이드

이 문서는 GitHub Actions CI/CD 파이프라인을 로컬 환경에서 테스트하기 위한 가이드입니다.

---

## 📋 목차

1. [사전 준비](#사전-준비)
2. [CI 파이프라인 로컬 테스트](#ci-파이프라인-로컬-테스트)
3. [CD 파이프라인 로컬 테스트](#cd-파이프라인-로컬-테스트)
4. [Docker 빌드 및 배포 테스트](#docker-빌드-및-배포-테스트)
5. [GitHub Actions 로컬 실행 (act)](#github-actions-로컬-실행-act)
6. [문제 해결](#문제-해결)

---

## 1. 사전 준비

### 필수 도구 설치

#### Windows 환경
```bash
# Java 21 설치 (Temurin)
winget install EclipseAdoptium.Temurin.21.JDK

# Maven 설치
winget install Apache.Maven

# Docker Desktop 설치
winget install Docker.DockerDesktop

# Git Bash 설치 (스크립트 실행용)
winget install Git.Git
```

#### 설치 확인
```bash
java -version      # Java 21 확인
mvn -version       # Maven 3.9+ 확인
docker --version   # Docker 확인
```

### 환경 변수 설정

프로젝트 루트에 `.env` 파일 생성:

```bash
# Database (PostgreSQL)
DB_USERNAME=postgres
DB_PASSWORD=password

# JWT
JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long-for-hs256-algorithm

# FastAPI
FASTAPI_BASE_URL=http://localhost:8000
FASTAPI_API_KEY=your-fastapi-api-key

# Oracle Cloud Object Storage (S3 호환)
S3_BUCKET_NAME=skax-reports
AWS_REGION=ap-northeast-2
AWS_ACCESS_KEY=your-oracle-access-key
AWS_SECRET_KEY=your-oracle-secret-key

# Kakao API
KAKAO_API_KEY=your-kakao-api-key

# Mail (SendGrid)
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key
```

> **참고**: Oracle Cloud Object Storage는 S3 호환 API를 제공하므로 기존 AWS SDK를 그대로 사용할 수 있습니다.

---

## 2. CI 파이프라인 로컬 테스트

CI 파이프라인은 코드 빌드, 테스트, 패키징을 수행합니다.

### Step 1: 빌드 (테스트 제외)

```bash
mvn clean compile -DskipTests
```

**예상 결과:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

### Step 2: 단위 테스트 실행

```bash
mvn test -Dspring.profiles.active=local
```

**예상 결과:**
```
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Step 3: 테스트 커버리지 리포트 생성

```bash
mvn jacoco:report
```

**결과 확인:**
```bash
# 브라우저로 열기
start target/site/jacoco/index.html  # Windows
```

### Step 4: 패키징 (JAR 생성)

```bash
mvn package -DskipTests
```

**생성된 파일 확인:**
```bash
ls -lh target/*.jar
# 예: physical-risk-management-1.0.0.jar
```

### 전체 CI 파이프라인 한번에 실행

```bash
# Windows (PowerShell)
mvn clean compile && mvn test && mvn jacoco:report && mvn package -DskipTests

# Git Bash / Linux / macOS
mvn clean compile && \
mvn test && \
mvn jacoco:report && \
mvn package -DskipTests
```

---

## 3. CD 파이프라인 로컬 테스트

CD 파이프라인은 Docker 이미지를 빌드하고 컨테이너로 배포합니다.

### 사전 확인

```bash
# Docker Desktop이 실행 중인지 확인
docker info

# PostgreSQL 컨테이너 실행 (로컬 DB 필요 시)
docker run -d \
  --name postgres-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=physical_risk_db \
  -p 5432:5432 \
  postgres:15-alpine
```

### 배포 스크립트 사용 (권장)

```bash
# Git Bash로 실행
bash docker-deploy.sh deploy
```

**또는 단계별 실행:**

```bash
# 1. 빌드만
bash docker-deploy.sh build

# 2. 기존 컨테이너 중지
bash docker-deploy.sh stop

# 3. 컨테이너 실행
bash docker-deploy.sh run

# 4. 로그 확인
bash docker-deploy.sh logs

# 5. 상태 확인
bash docker-deploy.sh status
```

---

## 4. Docker 빌드 및 배포 테스트

### 수동 Docker 명령어 사용

#### 1. Docker 이미지 빌드

```bash
docker build -t backend-springboot:latest .
```

**빌드 과정 확인:**
- Maven 빌드 (builder stage)
- JAR 파일 생성
- 최종 이미지 생성 (JRE 기반)

#### 2. 이미지 확인

```bash
docker images | grep backend-springboot
```

#### 3. 컨테이너 실행

```bash
docker run -d \
  --name backend-springboot \
  -p 8080:8080 \
  --env-file .env \
  --restart unless-stopped \
  backend-springboot:latest
```

#### 4. 컨테이너 상태 확인

```bash
# 실행 중인 컨테이너 확인
docker ps

# 로그 실시간 확인
docker logs -f backend-springboot

# 컨테이너 내부 접속
docker exec -it backend-springboot sh
```

#### 5. Health Check 확인

```bash
# Health check 엔드포인트 호출
curl http://localhost:8080/actuator/health

# 예상 응답:
# {"status":"UP"}
```

#### 6. API 테스트

```bash
# Swagger UI 접속
start http://localhost:8080/swagger-ui.html  # Windows

# 또는 curl로 테스트
curl http://localhost:8080/api/health
```

#### 7. 컨테이너 중지 및 삭제

```bash
docker stop backend-springboot
docker rm backend-springboot
```

---

## 5. GitHub Actions 로컬 실행 (act)

`act`를 사용하면 GitHub Actions를 로컬에서 실행할 수 있습니다.

### act 설치

```bash
# Windows (Chocolatey)
choco install act-cli

# Windows (Scoop)
scoop install act

# macOS
brew install act
```

### CI 워크플로우 로컬 실행

```bash
# 전체 워크플로우 실행
act

# 특정 job만 실행
act -j test

# 특정 이벤트 트리거
act push

# 환경변수 파일 사용
act --env-file .env.test

# 시크릿 파일 사용
act --secret-file .secrets
```

### 시크릿 파일 설정

`.secrets` 파일 생성:

```bash
JWT_SECRET=your-jwt-secret
FASTAPI_API_KEY=your-fastapi-key
GITHUB_TOKEN=your-github-token
```

### act 실행 예제

```bash
# CI 테스트 실행
act -j test --secret-file .secrets

# 빌드 job 실행
act -j build --secret-file .secrets

# Pull Request 이벤트 시뮬레이션
act pull_request
```

### act 문제 해결

```bash
# 대용량 러너 이미지 사용
act -P ubuntu-22.04=catthehacker/ubuntu:full-22.04

# Docker-in-Docker 볼륨 마운트
act --bind
```

---

## 6. 통합 테스트 시나리오

### 시나리오 1: 전체 CI/CD 파이프라인 테스트

```bash
# 1. 코드 변경 시뮬레이션
git checkout -b test/local-cicd

# 2. CI 테스트 실행
mvn clean test package

# 3. Docker 이미지 빌드
bash docker-deploy.sh build

# 4. 컨테이너 배포
bash docker-deploy.sh deploy

# 5. 헬스체크
curl http://localhost:8080/actuator/health

# 6. 정리
bash docker-deploy.sh stop
bash docker-deploy.sh cleanup
```

### 시나리오 2: Hot Reload 개발 환경

```bash
# 1. PostgreSQL 시작
docker run -d --name postgres-db \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  postgres:15-alpine

# 2. Spring Boot 로컬 실행 (DevTools)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3. 코드 수정 후 자동 재시작 확인
```

### 시나리오 3: 프로덕션 시뮬레이션

```bash
# 1. 프로덕션 프로파일로 빌드
mvn clean package -Dspring-boot.run.profiles=prod

# 2. Docker 이미지 빌드 (멀티스테이지)
docker build -t backend-springboot:prod .

# 3. 프로덕션 환경변수로 실행
docker run -d \
  --name backend-prod \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --env-file .env.prod \
  backend-springboot:prod

# 4. 모니터링
docker stats backend-prod
docker logs -f backend-prod
```

---

## 7. 문제 해결

### 문제 1: Maven 빌드 실패

```bash
# 캐시 정리
mvn clean

# 의존성 강제 업데이트
mvn clean install -U

# 오프라인 모드 비활성화
mvn clean install -DskipTests -U
```

### 문제 2: Docker 빌드 실패

```bash
# 빌드 캐시 무시
docker build --no-cache -t backend-springboot:latest .

# 빌드 로그 상세 출력
docker build --progress=plain -t backend-springboot:latest .

# 이전 이미지 정리
docker image prune -a -f
```

### 문제 3: 컨테이너 실행 실패

```bash
# 로그 확인
docker logs backend-springboot

# 컨테이너 내부 확인
docker exec -it backend-springboot sh

# 포트 충돌 확인
netstat -ano | findstr :8080  # Windows
lsof -i :8080                  # macOS/Linux

# 다른 포트로 실행
docker run -p 8081:8080 backend-springboot:latest
```

### 문제 4: 환경변수 로드 안됨

```bash
# .env 파일 확인
cat .env

# 환경변수 수동 설정
docker run -e JWT_SECRET=test -e FASTAPI_API_KEY=test ...

# application.yml 기본값 확인
cat src/main/resources/application-local.yml
```

### 문제 5: DB 연결 실패

```bash
# PostgreSQL 컨테이너 상태 확인
docker ps -a | grep postgres

# DB 연결 테스트
docker exec -it postgres-db psql -U postgres -c "\l"

# 네트워크 연결 확인
docker network ls
docker network inspect bridge
```

---

## 8. 유용한 명령어 모음

### Maven 명령어

```bash
# 의존성 트리 확인
mvn dependency:tree

# 특정 테스트만 실행
mvn test -Dtest=UserServiceTest

# 빌드 정보 출력
mvn help:effective-pom

# 플러그인 업데이트
mvn versions:display-plugin-updates
```

### Docker 명령어

```bash
# 모든 컨테이너 중지
docker stop $(docker ps -q)

# 사용하지 않는 리소스 정리
docker system prune -a

# 이미지 빌드 히스토리 확인
docker history backend-springboot:latest

# 컨테이너 리소스 사용량 확인
docker stats

# 로그 파일로 저장
docker logs backend-springboot > logs.txt 2>&1
```

### Git 명령어

```bash
# 로컬 브랜치에서 CI 트리거 시뮬레이션
git checkout -b feature/test
git commit --allow-empty -m "Test CI trigger"
git push origin feature/test

# 워크플로우 수동 트리거
gh workflow run ci_java.yaml
```

---

## 9. 성능 최적화 팁

### Maven 빌드 속도 향상

```bash
# 병렬 빌드
mvn clean install -T 4

# 오프라인 모드
mvn clean install -o

# 캐시 디렉토리 설정
mvn clean install -Dmaven.repo.local=./m2-cache
```

### Docker 빌드 속도 향상

```bash
# BuildKit 활성화 (Windows)
$env:DOCKER_BUILDKIT=1
docker build -t backend-springboot:latest .

# 빌드 캐시 마운트
docker build --cache-from backend-springboot:latest -t backend-springboot:latest .
```

---

## 10. CI/CD 체크리스트

로컬에서 배포 전 확인사항:

- [ ] 모든 테스트 통과 (`mvn test`)
- [ ] 빌드 성공 (`mvn package`)
- [ ] Docker 이미지 빌드 성공
- [ ] 컨테이너 정상 실행
- [ ] Health Check 통과
- [ ] API 엔드포인트 정상 응답
- [ ] 환경변수 올바르게 로드됨
- [ ] 로그에 에러 없음
- [ ] 데이터베이스 연결 성공
- [ ] 포트 충돌 없음

---

## 11. 실제 서버 배포

로컬에서 CI/CD 테스트를 완료한 후, 실제 Oracle Cloud 서버에 배포하려면 별도의 배포 가이드를 참조하세요.

**배포 가이드**: [ORACLE_SERVER_DEPLOY_GUIDE.md](ORACLE_SERVER_DEPLOY_GUIDE.md)

배포 가이드에서는 다음 내용을 다룹니다:
- Oracle Cloud 서버 환경 설정
- SSH 접속 및 서버 준비
- 프로덕션 환경 변수 설정
- 수동 배포 및 GitHub Actions 자동 배포
- Oracle Cloud Object Storage 설정
- 서버 모니터링 및 장애 대응

---

## 참고 자료

- [Maven 공식 문서](https://maven.apache.org/guides/)
- [Docker 공식 문서](https://docs.docker.com/)
- [GitHub Actions 문서](https://docs.github.com/en/actions)
- [act (로컬 GitHub Actions)](https://github.com/nektos/act)
- [Spring Boot Docker](https://spring.io/guides/topicals/spring-boot-docker/)

---

## 문의 및 지원

문제가 발생하면:

1. 로그 확인: `docker logs backend-springboot`
2. GitHub Issues 확인
3. 팀 개발자에게 문의

---

**작성일**: 2025-11-24
**버전**: 1.0.0
**작성자**: SKAX Team
