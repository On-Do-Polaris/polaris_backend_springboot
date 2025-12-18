#!/bin/bash

###############################################################################
# Java 프로젝트 빌드 스크립트
# Maven을 사용하여 프로젝트를 빌드합니다.
###############################################################################

set -e  # 에러 발생 시 스크립트 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# 시작 메시지
echo "=========================================="
echo "  Java 프로젝트 빌드 시작"
echo "=========================================="

# Java 버전 확인
log_info "Java 버전 확인 중..."
if ! command -v java &> /dev/null; then
    log_error "Java가 설치되어 있지 않습니다."
    exit 1
fi
java -version

# Maven 버전 확인
log_info "Maven 버전 확인 중..."
if ! command -v mvn &> /dev/null; then
    log_error "Maven이 설치되어 있지 않습니다."
    exit 1
fi
mvn -version

# 클린 빌드
log_info "프로젝트 클린 중..."
mvn clean

# 컴파일 (테스트 제외)
log_info "프로젝트 컴파일 중..."
mvn -B compile -DskipTests

# 코드 스타일 검사 (선택적)
log_info "코드 스타일 검사 중..."
mvn -B checkstyle:check || log_warn "Checkstyle 검사에서 경고가 발생했습니다."

# 테스트 실행 (선택적 - 환경변수로 제어)
if [ "${SKIP_TESTS}" != "true" ]; then
    log_info "테스트 실행 중..."
    mvn -B test -Dspring.profiles.active=local

    log_info "테스트 커버리지 리포트 생성 중..."
    mvn -B jacoco:report
else
    log_warn "테스트를 건너뜁니다."
fi

# 패키징
log_info "프로젝트 패키징 중..."
if [ "${SKIP_TESTS}" == "true" ]; then
    mvn -B package -DskipTests
else
    mvn -B package
fi

# 빌드 결과 확인
if [ -f target/*.jar ]; then
    log_info "빌드 성공!"
    log_info "생성된 JAR 파일:"
    ls -lh target/*.jar
else
    log_error "JAR 파일이 생성되지 않았습니다."
    exit 1
fi

echo "=========================================="
echo "  빌드 완료!"
echo "=========================================="
