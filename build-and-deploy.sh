#!/bin/bash

###############################################################################
# 빌드 + 배포 통합 스크립트
# 프로젝트를 빌드하고 Docker 이미지를 생성하여 GCP에 배포합니다.
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

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 시작 메시지
echo "=========================================="
echo "  빌드 + 배포 통합 프로세스 시작"
echo "=========================================="

# 스크립트 디렉토리로 이동
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 1. 빌드 실행
log_step "1/2 - 프로젝트 빌드 실행..."
if [ -f "./build.sh" ]; then
    bash ./build.sh
else
    log_error "build.sh 파일을 찾을 수 없습니다."
    exit 1
fi

echo ""
log_info "빌드가 성공적으로 완료되었습니다."
echo ""

# 2. 배포 실행
log_step "2/2 - Docker 이미지 빌드 및 배포 실행..."
if [ -f "./deploy.sh" ]; then
    bash ./deploy.sh
else
    log_error "deploy.sh 파일을 찾을 수 없습니다."
    exit 1
fi

# 완료 메시지
echo ""
echo "=========================================="
echo "  모든 작업이 완료되었습니다!"
echo "=========================================="
