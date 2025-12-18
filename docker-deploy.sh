#!/bin/bash

###############################################################################
# Docker 컨테이너 전체 라이프사이클 관리 스크립트
# - 기존 컨테이너/이미지 확인 및 삭제
# - Docker 이미지 빌드
# - 컨테이너 실행 및 Health Check
###############################################################################

set -e

# Configuration
IMAGE_NAME="${IMAGE_NAME:-polaris-backend-java}"
CONTAINER_NAME="${CONTAINER_NAME:-polaris-backend}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
HOST_PORT="${HOST_PORT:-8080}"
CONTAINER_PORT="${CONTAINER_PORT:-8080}"
NETWORK_NAME="${NETWORK_NAME:-polaris-network}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
ENV_FILE=".env"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
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

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

log_success() {
    echo -e "${CYAN}[SUCCESS]${NC} $1"
}

# Check if Docker is running
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker가 설치되어 있지 않습니다."
        exit 1
    fi

    if ! docker info > /dev/null 2>&1; then
        log_error "Docker가 실행 중이 아닙니다. Docker를 시작해주세요."
        exit 1
    fi
    log_info "Docker 실행 확인 완료"
    docker --version
}

# Build Docker image
build() {
    log_step "Docker 이미지 빌드 중..."
    log_info "이미지: ${IMAGE_NAME}:${IMAGE_TAG}"

    # 기존 이미지가 있으면 삭제
    if docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${IMAGE_NAME}:${IMAGE_TAG}$"; then
        log_warn "기존 이미지 발견: ${IMAGE_NAME}:${IMAGE_TAG}"
        log_info "기존 이미지 삭제 중..."
        docker rmi "${IMAGE_NAME}:${IMAGE_TAG}" 2>/dev/null || log_warn "이미지 삭제 실패 (사용 중일 수 있음)"
    fi

    docker build \
        --platform linux/amd64 \
        -t "${IMAGE_NAME}:${IMAGE_TAG}" \
        -f Dockerfile \
        .

    log_success "이미지 빌드 완료!"

    # 빌드된 이미지 정보 표시
    log_info "빌드된 이미지 정보:"
    docker images "${IMAGE_NAME}:${IMAGE_TAG}"
}

# Stop and remove existing container
stop() {
    log_step "기존 컨테이너 확인 및 삭제 중..."

    if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        log_warn "기존 컨테이너 발견: ${CONTAINER_NAME}"

        # 실행 중인지 확인
        if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
            log_info "컨테이너 중지 중..."
            docker stop "${CONTAINER_NAME}"
            log_success "컨테이너 중지 완료"
        fi

        # 컨테이너 삭제
        log_info "컨테이너 삭제 중..."
        docker rm "${CONTAINER_NAME}"
        log_success "컨테이너 삭제 완료"
    else
        log_info "기존 컨테이너가 없습니다."
    fi
}

# Create network if not exists
create_network() {
    log_step "Docker 네트워크 확인 중..."

    if ! docker network ls --format '{{.Name}}' | grep -q "^${NETWORK_NAME}$"; then
        log_info "네트워크 생성 중: ${NETWORK_NAME}"
        docker network create "${NETWORK_NAME}"
        log_success "네트워크 생성 완료"
    else
        log_info "기존 네트워크 사용: ${NETWORK_NAME}"
    fi
}

# Run container
run() {
    log_step "컨테이너 실행 중..."
    log_info "컨테이너 이름: ${CONTAINER_NAME}"
    log_info "포트 매핑: ${HOST_PORT}:${CONTAINER_PORT}"
    log_info "Spring Profile: ${SPRING_PROFILE}"

    # Check if .env file exists
    ENV_OPTION=""
    if [ -f "${ENV_FILE}" ]; then
        ENV_OPTION="--env-file ${ENV_FILE}"
        log_info ".env 파일 사용"
    else
        log_warn ".env 파일이 없습니다. 환경 변수 없이 실행합니다."
    fi

    docker run -d \
        --name "${CONTAINER_NAME}" \
        --network "${NETWORK_NAME}" \
        -p "${HOST_PORT}:${CONTAINER_PORT}" \
        -e SPRING_PROFILES_ACTIVE="${SPRING_PROFILE}" \
        ${ENV_OPTION} \
        --restart unless-stopped \
        "${IMAGE_NAME}:${IMAGE_TAG}"

    log_success "컨테이너 실행 완료!"
}

# Show logs
logs() {
    log_info "Showing logs for ${CONTAINER_NAME}..."
    docker logs -f ${CONTAINER_NAME}
}

# Health check
health_check() {
    log_step "애플리케이션 Health Check 중..."
    log_info "Health Check URL: http://localhost:${HOST_PORT}/actuator/health"

    # 컨테이너 시작 대기
    sleep 3

    # 최대 60초 대기
    RETRY_COUNT=0
    MAX_RETRIES=12

    while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
        if curl -f -s "http://localhost:${HOST_PORT}/actuator/health" > /dev/null 2>&1; then
            log_success "애플리케이션이 정상적으로 시작되었습니다!"
            echo ""
            curl -s "http://localhost:${HOST_PORT}/actuator/health" | grep -o '"status":"[^"]*"' || echo ""
            return 0
        else
            RETRY_COUNT=$((RETRY_COUNT + 1))
            echo -n "."
            sleep 5
        fi
    done

    echo ""
    log_warn "Health Check에 실패했습니다. 로그를 확인하세요."
    log_info "로그 확인: docker logs ${CONTAINER_NAME}"
    return 1
}

# Show status
status() {
    log_info "컨테이너 상태 확인 중..."

    if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        log_success "컨테이너 ${CONTAINER_NAME}이(가) 실행 중입니다."
        echo ""
        docker ps --filter "name=${CONTAINER_NAME}" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        echo ""
        log_info "로그 확인: docker logs -f ${CONTAINER_NAME}"
    else
        log_warn "컨테이너 ${CONTAINER_NAME}이(가) 실행 중이 아닙니다."

        # 중지된 컨테이너 확인
        if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
            log_info "중지된 컨테이너가 있습니다."
            docker ps -a --filter "name=${CONTAINER_NAME}" --format "table {{.Names}}\t{{.Status}}"
        fi
    fi
}

# Show recent logs
show_logs() {
    if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        log_info "최근 로그 (20줄):"
        echo "=========================================="
        docker logs --tail 20 "${CONTAINER_NAME}"
        echo "=========================================="
    else
        log_warn "컨테이너를 찾을 수 없습니다."
    fi
}

# Full deploy (build + stop + run)
deploy() {
    echo "=========================================="
    echo "  Docker 컨테이너 전체 배포"
    echo "=========================================="
    echo ""

    check_docker
    build
    echo ""

    stop
    echo ""

    create_network
    echo ""

    run
    echo ""

    status
    echo ""

    show_logs
    echo ""

    health_check
    echo ""

    echo "=========================================="
    echo "  배포 완료!"
    echo "=========================================="
    echo ""
    echo "애플리케이션 URL: http://localhost:${HOST_PORT}"
    echo "Health Check: http://localhost:${HOST_PORT}/actuator/health"
    echo "API 문서 (Swagger): http://localhost:${HOST_PORT}/swagger-ui.html"
    echo ""
    echo "유용한 명령어:"
    echo "  실시간 로그: docker logs -f ${CONTAINER_NAME}"
    echo "  컨테이너 중지: docker stop ${CONTAINER_NAME}"
    echo "  컨테이너 재시작: docker restart ${CONTAINER_NAME}"
    echo ""
    echo "=========================================="
}

# Clean up unused images
cleanup() {
    log_info "Cleaning up unused Docker images..."
    docker image prune -f
    log_info "Cleanup completed"
}

# Show help
help() {
    echo "=========================================="
    echo "  Docker 컨테이너 배포 스크립트"
    echo "=========================================="
    echo ""
    echo "사용법: $0 [command]"
    echo ""
    echo "명령어:"
    echo "  deploy       전체 배포 (빌드 + 중지 + 실행 + Health Check)"
    echo "  build        Docker 이미지 빌드만 실행"
    echo "  stop         컨테이너 중지 및 삭제"
    echo "  run          컨테이너 실행"
    echo "  logs         컨테이너 로그 확인 (실시간)"
    echo "  status       컨테이너 상태 확인"
    echo "  health       Health Check 실행"
    echo "  cleanup      사용하지 않는 Docker 이미지 정리"
    echo "  help         이 도움말 표시"
    echo ""
    echo "환경 변수:"
    echo "  IMAGE_NAME         이미지 이름 (기본값: polaris-backend-java)"
    echo "  CONTAINER_NAME     컨테이너 이름 (기본값: polaris-backend)"
    echo "  IMAGE_TAG          이미지 태그 (기본값: latest)"
    echo "  HOST_PORT          호스트 포트 (기본값: 8080)"
    echo "  CONTAINER_PORT     컨테이너 포트 (기본값: 8080)"
    echo "  SPRING_PROFILE     Spring Profile (기본값: prod)"
    echo "  NETWORK_NAME       Docker 네트워크 이름 (기본값: polaris-network)"
    echo ""
    echo "예제:"
    echo "  $0 deploy"
    echo "  HOST_PORT=9090 $0 deploy"
    echo "  SPRING_PROFILE=staging $0 deploy"
    echo ""
    echo "=========================================="
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
        create_network
        run
        status
        ;;
    logs)
        logs
        ;;
    status)
        status
        ;;
    health)
        health_check
        ;;
    cleanup)
        check_docker
        cleanup
        ;;
    help|--help|-h)
        help
        ;;
    *)
        log_error "알 수 없는 명령어: $1"
        echo ""
        help
        exit 1
        ;;
esac
