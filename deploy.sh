#!/bin/bash

###############################################################################
# Docker 이미지 빌드 및 GCP Artifact Registry 배포 스크립트
###############################################################################

set -e  # 에러 발생 시 스크립트 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 환경 변수 설정 (필수)
IMAGE_NAME="${IMAGE_NAME:-polaris-backend-java}"
ARTIFACT_REGISTRY_LOCATION="${ARTIFACT_REGISTRY_LOCATION}"
GCP_PROJECT_ID="${GCP_PROJECT_ID}"
ARTIFACT_REGISTRY_REPO="${ARTIFACT_REGISTRY_REPO}"

# 시작 메시지
echo "=========================================="
echo "  Docker 이미지 빌드 및 배포"
echo "=========================================="

# 환경 변수 확인
log_step "환경 변수 확인 중..."
if [ -z "$ARTIFACT_REGISTRY_LOCATION" ]; then
    log_error "ARTIFACT_REGISTRY_LOCATION 환경 변수가 설정되지 않았습니다."
    log_info "예: export ARTIFACT_REGISTRY_LOCATION=asia-northeast3"
    exit 1
fi

if [ -z "$GCP_PROJECT_ID" ]; then
    log_error "GCP_PROJECT_ID 환경 변수가 설정되지 않았습니다."
    log_info "예: export GCP_PROJECT_ID=your-project-id"
    exit 1
fi

if [ -z "$ARTIFACT_REGISTRY_REPO" ]; then
    log_error "ARTIFACT_REGISTRY_REPO 환경 변수가 설정되지 않았습니다."
    log_info "예: export ARTIFACT_REGISTRY_REPO=your-repo-name"
    exit 1
fi

log_info "이미지 이름: $IMAGE_NAME"
log_info "GCP 프로젝트: $GCP_PROJECT_ID"
log_info "레지스트리 위치: $ARTIFACT_REGISTRY_LOCATION"
log_info "레지스트리 저장소: $ARTIFACT_REGISTRY_REPO"

# Docker 확인
log_step "Docker 설치 확인 중..."
if ! command -v docker &> /dev/null; then
    log_error "Docker가 설치되어 있지 않습니다."
    exit 1
fi
docker --version

# gcloud 확인
log_step "gcloud CLI 설치 확인 중..."
if ! command -v gcloud &> /dev/null; then
    log_error "gcloud CLI가 설치되어 있지 않습니다."
    log_info "설치: https://cloud.google.com/sdk/docs/install"
    exit 1
fi
gcloud --version

# GCP 인증 확인
log_step "GCP 인증 확인 중..."
CURRENT_PROJECT=$(gcloud config get-value project 2>/dev/null)
if [ -z "$CURRENT_PROJECT" ]; then
    log_error "GCP 프로젝트가 설정되지 않았습니다."
    log_info "인증: gcloud auth login"
    log_info "프로젝트 설정: gcloud config set project YOUR_PROJECT_ID"
    exit 1
fi
log_info "현재 GCP 프로젝트: $CURRENT_PROJECT"

# 프로젝트 ID 일치 확인
if [ "$CURRENT_PROJECT" != "$GCP_PROJECT_ID" ]; then
    log_warn "현재 프로젝트($CURRENT_PROJECT)와 지정된 프로젝트($GCP_PROJECT_ID)가 다릅니다."
    log_info "프로젝트 변경 중..."
    gcloud config set project "$GCP_PROJECT_ID"
fi

# Artifact Registry 인증
log_step "Artifact Registry 인증 설정 중..."
gcloud auth configure-docker "${ARTIFACT_REGISTRY_LOCATION}-docker.pkg.dev" --quiet

# 이미지 태그 생성
IMAGE_TAG="latest"
FULL_IMAGE_NAME="${ARTIFACT_REGISTRY_LOCATION}-docker.pkg.dev/${GCP_PROJECT_ID}/${ARTIFACT_REGISTRY_REPO}/${IMAGE_NAME}:${IMAGE_TAG}"

log_info "최종 이미지 태그: $FULL_IMAGE_NAME"

# Docker 이미지 빌드
log_step "Docker 이미지 빌드 중..."
docker build \
    --platform linux/amd64 \
    -t "$FULL_IMAGE_NAME" \
    -f Dockerfile \
    .

log_info "이미지 빌드 완료!"

# 빌드된 이미지 확인
log_step "빌드된 이미지 정보 확인..."
docker images "$FULL_IMAGE_NAME"

# Docker 이미지 푸시
log_step "Docker 이미지를 Artifact Registry에 푸시 중..."
docker push "$FULL_IMAGE_NAME"

log_info "이미지 푸시 완료!"

# 완료 메시지
echo "=========================================="
echo "  배포 완료!"
echo "=========================================="
echo ""
echo "Registry: ${ARTIFACT_REGISTRY_LOCATION}-docker.pkg.dev"
echo "Project: ${GCP_PROJECT_ID}"
echo "Repository: ${ARTIFACT_REGISTRY_REPO}"
echo "Image: ${FULL_IMAGE_NAME}"
echo ""
echo "=========================================="
