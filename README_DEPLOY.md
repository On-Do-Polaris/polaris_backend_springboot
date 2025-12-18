# 빌드 및 배포 스크립트 사용 가이드

이 프로젝트는 셸 스크립트를 사용하여 빌드 및 배포를 수행할 수 있습니다.

## 스크립트 파일

### 로컬 Docker 배포 (권장)
- **`docker-deploy.sh`** - Docker 컨테이너 전체 라이프사이클 관리 (추천!)
  - 기존 컨테이너/이미지 확인 및 삭제
  - Docker 이미지 빌드
  - 컨테이너 실행 및 Health Check
  - 로그 확인 및 상태 모니터링

### Maven 빌드 및 GCP 배포
- `build.sh` - Java 프로젝트 빌드
- `deploy.sh` - Docker 이미지 빌드 및 GCP Artifact Registry 배포
- `build-and-deploy.sh` - 빌드와 배포를 한 번에 실행

## 사전 준비

### 1. 필수 도구 설치

- Java 21 (JDK)
- Maven 3.x
- Docker
- gcloud CLI (GCP 배포 시)

### 2. 스크립트 실행 권한 부여

```bash
chmod +x build.sh deploy.sh build-and-deploy.sh
```

### 3. 환경 변수 설정

`.env` 파일을 생성하거나 환경 변수를 직접 설정합니다.

```bash
# .env.example을 복사하여 .env 생성
cp .env.example .env

# .env 파일 편집
vi .env
```

필수 환경 변수:
```bash
# GCP 설정
export ARTIFACT_REGISTRY_LOCATION=asia-northeast3
export GCP_PROJECT_ID=your-project-id
export ARTIFACT_REGISTRY_REPO=your-repo-name

# 선택 사항
export IMAGE_NAME=polaris-backend-java
export SKIP_TESTS=false
```

또는 직접 export:
```bash
export ARTIFACT_REGISTRY_LOCATION=asia-northeast3
export GCP_PROJECT_ID=your-project-id
export ARTIFACT_REGISTRY_REPO=your-repo-name
```

## 빠른 시작 (로컬 Docker)

가장 간단한 방법은 `docker-deploy.sh`를 사용하는 것입니다.

### 1. 실행 권한 부여

```bash
chmod +x docker-deploy.sh
```

### 2. 전체 배포 실행

```bash
./docker-deploy.sh
```

또는 명령어를 생략하면 기본적으로 `deploy` 실행:

```bash
./docker-deploy.sh deploy
```

이 명령어는 다음을 자동으로 수행합니다:
1. Docker 실행 확인
2. 기존 컨테이너 중지 및 삭제
3. 기존 이미지 삭제
4. 새 Docker 이미지 빌드
5. Docker 네트워크 생성/확인
6. 컨테이너 실행
7. Health Check 수행
8. 로그 확인

### 3. 개별 명령어 사용

```bash
# 빌드만
./docker-deploy.sh build

# 컨테이너 중지 및 삭제
./docker-deploy.sh stop

# 컨테이너 실행
./docker-deploy.sh run

# 로그 확인 (실시간)
./docker-deploy.sh logs

# 상태 확인
./docker-deploy.sh status

# Health Check
./docker-deploy.sh health

# 도움말
./docker-deploy.sh help
```

### 4. 환경 변수로 커스터마이징

```bash
# 다른 포트로 실행
HOST_PORT=9090 ./docker-deploy.sh

# Staging 프로필로 실행
SPRING_PROFILE=staging ./docker-deploy.sh

# 커스텀 컨테이너 이름
CONTAINER_NAME=my-backend ./docker-deploy.sh

# 모든 옵션 조합
HOST_PORT=9090 SPRING_PROFILE=staging CONTAINER_NAME=my-app ./docker-deploy.sh
```

## Maven 빌드 스크립트 사용 방법

### 1. 빌드만 실행

```bash
./build.sh
```

기능:
- Maven clean
- 컴파일
- Checkstyle 검사
- 테스트 실행 (SKIP_TESTS=false인 경우)
- 테스트 커버리지 리포트 생성
- JAR 파일 패키징

테스트를 건너뛰려면:
```bash
SKIP_TESTS=true ./build.sh
```

### 2. 배포만 실행

```bash
./deploy.sh
```

기능:
- Docker 및 gcloud CLI 확인
- GCP 인증 확인
- Artifact Registry 인증 설정
- Docker 이미지 빌드
- Docker 이미지 푸시

### 3. 빌드 + 배포 통합 실행

```bash
./build-and-deploy.sh
```

빌드와 배포를 순차적으로 실행합니다.

## GCP 인증 설정

배포 스크립트 실행 전에 GCP 인증이 필요합니다.

### 방법 1: gcloud 로그인

```bash
# GCP 계정으로 로그인
gcloud auth login

# 프로젝트 설정
gcloud config set project YOUR_PROJECT_ID

# Application Default Credentials 설정
gcloud auth application-default login
```

### 방법 2: Service Account 사용

```bash
# Service Account 키 파일을 사용하여 인증
gcloud auth activate-service-account --key-file=path/to/service-account-key.json

# 프로젝트 설정
gcloud config set project YOUR_PROJECT_ID
```

## docker-deploy.sh 상세 설정

### 환경 변수 목록

| 환경 변수 | 기본값 | 설명 |
|----------|--------|------|
| `IMAGE_NAME` | `polaris-backend-java` | Docker 이미지 이름 |
| `CONTAINER_NAME` | `polaris-backend` | Docker 컨테이너 이름 |
| `IMAGE_TAG` | `latest` | Docker 이미지 태그 |
| `HOST_PORT` | `8080` | 호스트 포트 |
| `CONTAINER_PORT` | `8080` | 컨테이너 포트 |
| `SPRING_PROFILE` | `prod` | Spring 프로필 (local/staging/prod) |
| `NETWORK_NAME` | `polaris-network` | Docker 네트워크 이름 |

### .env 파일 사용

프로젝트 루트에 `.env` 파일이 있으면 자동으로 컨테이너에 전달됩니다.

```bash
# .env.example을 복사
cp .env.example .env

# .env 파일 편집
vi .env
```

### Health Check 기능

스크립트는 컨테이너 실행 후 자동으로 Health Check를 수행합니다:
- URL: `http://localhost:8080/actuator/health`
- 최대 대기 시간: 60초
- 재시도 간격: 5초

Health Check가 실패하면 로그를 확인하세요:
```bash
docker logs polaris-backend
```

## 트러블슈팅

### Docker 권한 오류

```bash
# Docker 그룹에 사용자 추가
sudo usermod -aG docker $USER

# 로그아웃 후 다시 로그인하거나 다음 명령 실행
newgrp docker
```

### gcloud 인증 오류

```bash
# 현재 인증 정보 확인
gcloud auth list

# 프로젝트 확인
gcloud config get-value project

# 재인증
gcloud auth login
gcloud auth application-default login
```

### Maven 빌드 실패

```bash
# Maven 캐시 정리
mvn clean

# 오프라인 의존성 다운로드
mvn dependency:resolve

# 강제 업데이트
mvn clean install -U
```

## CI/CD vs 로컬 배포

- **CI/CD (GitHub Actions)**: `.github/workflows/ci_java.yaml` 사용
- **로컬 배포**: 이 스크립트 파일들 사용

두 방식 모두 동일한 Docker 이미지를 GCP Artifact Registry에 배포합니다.

## 환경별 배포

### 개발 환경
```bash
export SPRING_PROFILES_ACTIVE=local
./build.sh
```

### 스테이징 환경
```bash
export SPRING_PROFILES_ACTIVE=staging
./build-and-deploy.sh
```

### 프로덕션 환경
```bash
export SPRING_PROFILES_ACTIVE=prod
SKIP_TESTS=false ./build-and-deploy.sh
```

### 컨테이너가 시작되지 않는 경우

```bash
# 로그 확인
./docker-deploy.sh logs

# 또는 직접 확인
docker logs polaris-backend

# 컨테이너 상태 확인
./docker-deploy.sh status
```

### 포트가 이미 사용 중인 경우

```bash
# 다른 포트 사용
HOST_PORT=9090 ./docker-deploy.sh

# 또는 기존 프로세스 종료
lsof -ti:8080 | xargs kill -9  # Mac/Linux
netstat -ano | findstr :8080   # Windows (포트 사용 프로세스 확인)
```

### 컨테이너 완전 초기화

```bash
# 컨테이너 중지 및 삭제
./docker-deploy.sh stop

# 이미지 삭제
docker rmi polaris-backend-java:latest

# 네트워크 삭제
docker network rm polaris-network

# 다시 배포
./docker-deploy.sh
```

## 유용한 Docker 명령어

```bash
# 실행 중인 모든 컨테이너 확인
docker ps

# 모든 컨테이너 확인 (중지된 것 포함)
docker ps -a

# 컨테이너 로그 실시간 확인
docker logs -f polaris-backend

# 컨테이너 내부 접속
docker exec -it polaris-backend /bin/sh

# 컨테이너 재시작
docker restart polaris-backend

# 컨테이너 중지
docker stop polaris-backend

# 네트워크 확인
docker network ls

# 사용하지 않는 리소스 정리
docker system prune -a
```

## 주의사항

1. **로컬 개발**: `docker-deploy.sh` 사용 (간편하고 빠름)
2. **GCP 배포**: `deploy.sh` 또는 CI/CD 파이프라인 사용
3. 프로덕션 배포 시 테스트를 반드시 실행하세요 (SKIP_TESTS=false)
4. GCP 인증 정보를 안전하게 관리하세요
5. 환경 변수를 올바르게 설정했는지 확인하세요
6. Docker 데몬이 실행 중인지 확인하세요

## 참고

- Docker 이미지는 `linux/amd64` 플랫폼으로 빌드됩니다
- 이미지 태그는 기본적으로 `latest`로 설정됩니다
- 빌드된 JAR 파일은 `target/` 디렉토리에 생성됩니다
