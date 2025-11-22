# Backend SpringBoot CI/CD 가이드

이 문서는 `backend_springboot` 저장소의 CI/CD 파이프라인 구축 가이드입니다.

## 1. CI/CD 파이프라인 개요

```
Push to main
     │
     ▼
┌─────────────────────────────────────┐
│  CI (ci_java.yaml)                  │
│  1. test job: 빌드 + 테스트         │
│  2. build job: docker-build.sh ci   │
│     → 이미지 빌드 + ghcr.io Push    │
└─────────────────────────────────────┘
     │ (CI 성공 시)
     ▼
┌─────────────────────────────────────┐
│  CD (cd_java.yaml)                  │
│  1. SSH로 서버 접속                 │
│  2. docker-deploy.sh deploy 실행    │
│     → 새 이미지 Pull + 컨테이너 재시작│
└─────────────────────────────────────┘
```

## 2. 필요한 파일 구조

```
backend_springboot/
├── .github/
│   └── workflows/
│       ├── ci_java.yaml        # CI 워크플로우
│       └── cd_java.yaml        # CD 워크플로우
├── src/
│   └── main/
│       └── java/
│           └── ...
├── build.gradle                # Gradle 빌드 파일 (또는 pom.xml)
├── Dockerfile                  # Docker 이미지 빌드
├── docker-build.sh             # 이미지 빌드 스크립트
├── docker-deploy.sh            # 배포 스크립트
└── .env.example
```

## 3. 파일별 상세 내용

### 3.1 Dockerfile (Gradle 기준)

```dockerfile
# Build stage
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Copy gradle files first for caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (캐싱 활용)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build application
RUN gradle build -x test --no-daemon

# Production stage
FROM eclipse-temurin:17-jre-alpine AS production

WORKDIR /app

# Copy built jar from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser && \
    chown -R appuser:appgroup /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3.2 Dockerfile (Maven 기준)

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first for caching
COPY pom.xml .

# Download dependencies (캐싱 활용)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn package -DskipTests -B

# Production stage
FROM eclipse-temurin:17-jre-alpine AS production

WORKDIR /app

# Copy built jar from builder
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser && \
    chown -R appuser:appgroup /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3.3 docker-build.sh

```bash
#!/bin/bash

# =============================================================================
# Docker Build Script for Backend SpringBoot
# CI에서 이미지 빌드 및 레지스트리 Push용
# =============================================================================

set -e

# Configuration (환경변수로 오버라이드 가능)
REGISTRY="${REGISTRY:-ghcr.io}"
IMAGE_NAME="${IMAGE_NAME:-backend-springboot}"
TAG="${TAG:-latest}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running"
        exit 1
    fi
    log_info "Docker is running"
}

# Login to registry
login() {
    if [ -z "${REGISTRY_USERNAME}" ] || [ -z "${REGISTRY_PASSWORD}" ]; then
        log_warn "REGISTRY_USERNAME or REGISTRY_PASSWORD not set. Skipping login."
        return 0
    fi

    log_info "Logging in to ${REGISTRY}..."
    echo "${REGISTRY_PASSWORD}" | docker login ${REGISTRY} -u "${REGISTRY_USERNAME}" --password-stdin
    log_info "Login successful"
}

# Build Docker image
build() {
    local full_image="${REGISTRY}/${GITHUB_REPOSITORY:-local}/${IMAGE_NAME}:${TAG}"

    log_info "Building Docker image: ${full_image}"

    docker build \
        --tag "${full_image}" \
        --tag "${REGISTRY}/${GITHUB_REPOSITORY:-local}/${IMAGE_NAME}:latest" \
        --label "org.opencontainers.image.source=https://github.com/${GITHUB_REPOSITORY:-local}" \
        --label "org.opencontainers.image.revision=${GITHUB_SHA:-unknown}" \
        .

    log_info "Build completed: ${full_image}"
}

# Push to registry
push() {
    local full_image="${REGISTRY}/${GITHUB_REPOSITORY:-local}/${IMAGE_NAME}:${TAG}"

    log_info "Pushing image: ${full_image}"
    docker push "${full_image}"

    log_info "Pushing latest tag..."
    docker push "${REGISTRY}/${GITHUB_REPOSITORY:-local}/${IMAGE_NAME}:latest"

    log_info "Push completed"
}

# Full CI build (login + build + push)
ci_build() {
    log_info "Starting CI build process..."
    check_docker
    login
    build
    push
    log_info "CI build completed successfully!"
}

# Local build only (no push)
local_build() {
    log_info "Starting local build..."
    check_docker

    docker build \
        --tag "${IMAGE_NAME}:${TAG}" \
        --tag "${IMAGE_NAME}:latest" \
        .

    log_info "Local build completed: ${IMAGE_NAME}:${TAG}"
}

# Show help
help() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  ci        Full CI build (login + build + push)"
    echo "  local     Local build only (no push)"
    echo "  help      Show this help message"
}

# Main
case "${1:-help}" in
    ci)
        ci_build
        ;;
    local)
        local_build
        ;;
    help|--help|-h)
        help
        ;;
    *)
        log_error "Unknown command: $1"
        help
        exit 1
        ;;
esac
```

### 3.4 docker-deploy.sh

```bash
#!/bin/bash

# =============================================================================
# Docker Deploy Script for Backend SpringBoot
# =============================================================================

set -e

# Configuration
IMAGE_NAME="backend-springboot"
CONTAINER_NAME="backend-springboot"
PORT="${PORT:-8080}"
ENV_FILE=".env"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    log_info "Docker is running"
}

# Build Docker image
build() {
    log_info "Building Docker image: ${IMAGE_NAME}..."
    docker build -t ${IMAGE_NAME}:latest .
    log_info "Build completed successfully"
}

# Stop and remove existing container
stop() {
    if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        log_info "Stopping existing container: ${CONTAINER_NAME}..."
        docker stop ${CONTAINER_NAME} 2>/dev/null || true
        docker rm ${CONTAINER_NAME} 2>/dev/null || true
        log_info "Container stopped and removed"
    else
        log_info "No existing container found"
    fi
}

# Run container
run() {
    log_info "Starting container: ${CONTAINER_NAME}..."

    # Check if .env file exists
    ENV_OPTION=""
    if [ -f "${ENV_FILE}" ]; then
        ENV_OPTION="--env-file ${ENV_FILE}"
        log_info "Using environment file: ${ENV_FILE}"
    else
        log_warn "No .env file found. Running without environment file."
    fi

    docker run -d \
        --name ${CONTAINER_NAME} \
        -p ${PORT}:8080 \
        ${ENV_OPTION} \
        --restart unless-stopped \
        ${IMAGE_NAME}:latest

    log_info "Container started on port ${PORT}"
}

# Show logs
logs() {
    log_info "Showing logs for ${CONTAINER_NAME}..."
    docker logs -f ${CONTAINER_NAME}
}

# Show status
status() {
    if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        log_info "Container ${CONTAINER_NAME} is running"
        docker ps --filter "name=${CONTAINER_NAME}" --format "table {{.ID}}\t{{.Status}}\t{{.Ports}}"
    else
        log_warn "Container ${CONTAINER_NAME} is not running"
    fi
}

# Full deploy (build + stop + run)
deploy() {
    log_info "Starting full deployment..."
    check_docker
    build
    stop
    run
    log_info "Deployment completed successfully!"
    status
}

# Clean up unused images
cleanup() {
    log_info "Cleaning up unused Docker images..."
    docker image prune -f
    log_info "Cleanup completed"
}

# Show help
help() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  deploy    Full deployment (build + stop + run)"
    echo "  build     Build Docker image only"
    echo "  stop      Stop and remove container"
    echo "  run       Run container"
    echo "  logs      Show container logs"
    echo "  status    Show container status"
    echo "  cleanup   Remove unused Docker images"
    echo "  help      Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  PORT      Host port to expose (default: 8080)"
}

# Main
case "${1:-deploy}" in
    deploy)
        deploy
        ;;
    build)
        check_docker
        build
        ;;
    stop)
        check_docker
        stop
        ;;
    run)
        check_docker
        run
        ;;
    logs)
        logs
        ;;
    status)
        status
        ;;
    cleanup)
        check_docker
        cleanup
        ;;
    help|--help|-h)
        help
        ;;
    *)
        log_error "Unknown command: $1"
        help
        exit 1
        ;;
esac
```

### 3.5 .github/workflows/ci_java.yaml (Gradle 기준)

```yaml
name: CI - Test & Build

on:
  push:
    branches: [develop, main]
  pull_request:
    branches: [main, develop]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: backend-springboot

jobs:
  test:
    name: Test & Build
    runs-on: ubuntu-22.04

    steps:
      - name: 코드 체크아웃
        uses: actions/checkout@v4

      - name: JDK 17 설치
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Gradle 실행 권한 부여
        run: chmod +x gradlew

      - name: 빌드 및 테스트
        run: ./gradlew build

      - name: 테스트 리포트 업로드
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: build/reports/tests/

  build:
    name: Build & Push Image
    runs-on: ubuntu-22.04
    needs: test
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'

    permissions:
      contents: read
      packages: write

    steps:
      - name: 코드 체크아웃
        uses: actions/checkout@v4

      - name: Docker Buildx 설정
        uses: docker/setup-buildx-action@v3

      - name: GitHub Container Registry 로그인
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: 스크립트 실행 권한 부여
        run: chmod +x ./docker-build.sh

      - name: Docker 이미지 빌드 및 Push
        env:
          REGISTRY: ${{ env.REGISTRY }}
          REGISTRY_USERNAME: ${{ github.actor }}
          REGISTRY_PASSWORD: ${{ secrets.GITHUB_TOKEN }}
          GITHUB_REPOSITORY: ${{ github.repository }}
          GITHUB_SHA: ${{ github.sha }}
          TAG: ${{ github.sha }}
        run: ./docker-build.sh ci

      - name: 빌드 완료 알림
        run: |
          echo "✅ Docker image built and pushed successfully!"
          echo "Image: ${{ env.REGISTRY }}/${{ github.repository }}/${{ env.IMAGE_NAME }}:${{ github.sha }}"
```

### 3.6 .github/workflows/ci_java.yaml (Maven 기준)

```yaml
name: CI - Test & Build

on:
  push:
    branches: [develop, main]
  pull_request:
    branches: [main, develop]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: backend-springboot

jobs:
  test:
    name: Test & Build
    runs-on: ubuntu-22.04

    steps:
      - name: 코드 체크아웃
        uses: actions/checkout@v4

      - name: JDK 17 설치
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'

      - name: 빌드 및 테스트
        run: mvn -B package

      - name: 테스트 리포트 업로드
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: target/surefire-reports/

  build:
    name: Build & Push Image
    runs-on: ubuntu-22.04
    needs: test
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'

    permissions:
      contents: read
      packages: write

    steps:
      - name: 코드 체크아웃
        uses: actions/checkout@v4

      - name: Docker Buildx 설정
        uses: docker/setup-buildx-action@v3

      - name: GitHub Container Registry 로그인
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: 스크립트 실행 권한 부여
        run: chmod +x ./docker-build.sh

      - name: Docker 이미지 빌드 및 Push
        env:
          REGISTRY: ${{ env.REGISTRY }}
          REGISTRY_USERNAME: ${{ github.actor }}
          REGISTRY_PASSWORD: ${{ secrets.GITHUB_TOKEN }}
          GITHUB_REPOSITORY: ${{ github.repository }}
          GITHUB_SHA: ${{ github.sha }}
          TAG: ${{ github.sha }}
        run: ./docker-build.sh ci

      - name: 빌드 완료 알림
        run: |
          echo "✅ Docker image built and pushed successfully!"
          echo "Image: ${{ env.REGISTRY }}/${{ github.repository }}/${{ env.IMAGE_NAME }}:${{ github.sha }}"
```

### 3.7 .github/workflows/cd_java.yaml

```yaml
name: CD - Deploy

on:
  workflow_run:
    workflows: ["CI - Test & Build"]
    types: [completed]
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: backend-springboot

jobs:
  deploy:
    name: Deploy to Server
    runs-on: ubuntu-22.04
    if: ${{ github.event.workflow_run.conclusion == 'success' }}

    steps:
      - name: 코드 체크아웃
        uses: actions/checkout@v4

      - name: 서버에 배포
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USERNAME }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          port: ${{ secrets.SERVER_PORT || 22 }}
          script: |
            cd ${{ secrets.DEPLOY_PATH }}

            # 최신 코드 가져오기
            git pull origin main

            # 스크립트 실행 권한 부여
            chmod +x ./docker-deploy.sh

            # 배포 실행
            ./docker-deploy.sh deploy

            echo "✅ Deployment completed!"

      - name: 배포 완료 알림
        if: success()
        run: |
          echo "✅ Deployment to server completed successfully!"

      - name: 배포 실패 알림
        if: failure()
        run: |
          echo "❌ Deployment failed!"
          exit 1
```

## 4. GitHub Secrets 설정

CD가 작동하려면 다음 Secrets를 GitHub 저장소에 설정해야 합니다:

| Secret Name | 설명 | 예시 |
|-------------|------|------|
| `SERVER_HOST` | 배포 서버 IP/도메인 | `123.456.789.0` |
| `SERVER_USERNAME` | SSH 사용자명 | `ubuntu` |
| `SERVER_SSH_KEY` | SSH 개인키 | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `SERVER_PORT` | SSH 포트 (선택, 기본값: 22) | `22` |
| `DEPLOY_PATH` | 서버의 프로젝트 경로 | `/opt/backend_springboot` |

### Secrets 설정 방법

1. GitHub 저장소 → **Settings** → **Secrets and variables** → **Actions**
2. **"New repository secret"** 클릭
3. Name과 Secret 값 입력 후 저장

## 5. Python vs Java CI/CD 비교

| 항목 | Python (FastAPI) | Java (SpringBoot) |
|------|------------------|-------------------|
| **런타임** | Python 3.11 | JDK 17 |
| **패키지 매니저** | uv | Gradle / Maven |
| **의존성 설치** | `uv sync` | `gradle build` / `mvn package` |
| **린트** | Black, Flake8 | Checkstyle, SpotBugs (선택) |
| **테스트** | pytest | JUnit |
| **베이스 이미지** | python:3.11-slim | eclipse-temurin:17-jre-alpine |
| **실행 명령** | `uvicorn main:app` | `java -jar app.jar` |
| **기본 포트** | 8000 | 8080 |
| **Health Check** | `/health` | `/actuator/health` |

## 6. 로컬 테스트

### 6.1 이미지 빌드 테스트

```bash
# 스크립트 실행 권한 부여
chmod +x docker-build.sh docker-deploy.sh

# 로컬 빌드
./docker-build.sh local
```

### 6.2 컨테이너 실행 테스트

```bash
# 배포 (빌드 + 실행)
./docker-deploy.sh deploy

# 로그 확인
./docker-deploy.sh logs

# 상태 확인
./docker-deploy.sh status

# 중지
./docker-deploy.sh stop
```

### 6.3 Health Check 확인

```bash
curl http://localhost:8080/actuator/health
```

## 7. 주의사항

### 7.1 application.yml 설정

Health Check를 위해 Spring Actuator가 활성화되어 있어야 합니다:

```yaml
# src/main/resources/application.yml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never
```

### 7.2 build.gradle 의존성

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

### 7.3 pom.xml 의존성 (Maven)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## 8. 트러블슈팅

### Gradle 빌드 실패

```bash
# 로컬에서 빌드 테스트
./gradlew clean build

# 권한 문제 시
chmod +x gradlew
```

### Maven 빌드 실패

```bash
# 로컬에서 빌드 테스트
mvn clean package

# 의존성 문제 시
mvn dependency:resolve
```

### Docker 이미지 빌드 실패

1. Dockerfile의 JAR 파일 경로 확인
2. `build/libs/*.jar` (Gradle) 또는 `target/*.jar` (Maven)
3. 빌드 시 `-x test`로 테스트 스킵 여부 확인

### Health Check 실패

1. Actuator 의존성 확인
2. `/actuator/health` 엔드포인트 활성화 확인
3. 애플리케이션 시작 시간 고려 (`start-period` 조정)
