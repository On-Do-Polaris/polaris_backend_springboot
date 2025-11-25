# 🚀 Oracle 서버 배포 가이드

이 문서는 Oracle Cloud Infrastructure (OCI) 서버에 Spring Boot 애플리케이션을 배포하는 가이드입니다.

> **로컬 테스트**: 배포 전 로컬에서 CI/CD 테스트를 먼저 진행하세요. → [LOCAL_CICD_TEST_GUIDE.md](LOCAL_CICD_TEST_GUIDE.md)

---

## 📋 목차

1. [사전 준비](#사전-준비)
2. [Oracle 서버 환경 설정](#oracle-서버-환경-설정)
3. [수동 배포](#수동-배포)
4. [GitHub Actions 자동 배포](#github-actions-자동-배포)
5. [Oracle Cloud Object Storage 설정](#oracle-cloud-object-storage-설정)
6. [서버 모니터링](#서버-모니터링)
7. [장애 대응 및 롤백](#장애-대응-및-롤백)
8. [보안 강화](#보안-강화)

---

## 1. 사전 준비

### Oracle Cloud 계정 및 서버

- Oracle Cloud 계정 (Free Tier 가능)
- Compute Instance (VM) 생성 완료
- SSH 키 페어 생성 및 보관
- 서버 Public IP 주소 확인

### 필요한 정보

```
서버 IP: xxx.xxx.xxx.xxx
SSH 키: oracle-key.pem
사용자명: opc (Oracle Linux 기본 사용자)
포트: 22 (SSH)
```

---

## 2. Oracle 서버 환경 설정

### Step 1: 서버 접속

#### Windows 환경

```bash
# PowerShell
ssh -i C:\Users\YourName\.ssh\oracle-key.pem opc@your-server-ip

# Git Bash
ssh -i /c/Users/YourName/.ssh/oracle-key.pem opc@your-server-ip
```

#### Linux / macOS

```bash
# SSH 키 권한 설정 (최초 1회)
chmod 600 ~/.ssh/oracle-key.pem

# 서버 접속
ssh -i ~/.ssh/oracle-key.pem opc@your-server-ip
```

### Step 2: Docker 설치

```bash
# Docker 설치 확인
docker --version

# Docker가 없으면 설치
sudo yum install -y docker-engine
sudo systemctl start docker
sudo systemctl enable docker

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# 로그아웃 후 재접속하여 권한 적용
exit
# 다시 SSH 접속

# Docker 설치 확인
docker ps
```

### Step 3: Git 설치 및 프로젝트 클론

```bash
# Git 설치 확인
git --version

# Git이 없으면 설치
sudo yum install -y git

# 프로젝트 클론 (최초 1회)
cd /home/opc
git clone https://github.com/your-org/backend_team_java.git
cd backend_team_java
```

### Step 4: 환경 변수 설정

```bash
# 서버에 .env 파일 생성
vi .env
```

**`.env` 파일 내용** (프로덕션 환경):

```bash
# =============================================================================
# SKALA Physical Risk AI - Backend SpringBoot 프로덕션 환경변수
# =============================================================================

# -----------------------------------------------------------------------------
# 서버 설정
# -----------------------------------------------------------------------------
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# -----------------------------------------------------------------------------
# Database (PostgreSQL)
# -----------------------------------------------------------------------------
DB_HOST=your-db-host.oraclecloud.com
DB_PORT=5432
DB_USERNAME=prod_user
DB_PASSWORD=your-production-db-password

# -----------------------------------------------------------------------------
# Redis
# -----------------------------------------------------------------------------
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password

# -----------------------------------------------------------------------------
# JWT (강력한 시크릿 키 사용 - 최소 256비트)
# -----------------------------------------------------------------------------
JWT_SECRET=your-production-super-secret-jwt-key-at-least-256-bits-long-change-this

# -----------------------------------------------------------------------------
# FastAPI AI Agent 설정
# -----------------------------------------------------------------------------
# 같은 서버인 경우: http://localhost:8000
# 다른 서버인 경우: http://fastapi-server-ip:8000
FASTAPI_BASE_URL=http://localhost:8000
FASTAPI_API_KEY=your-production-fastapi-key

# -----------------------------------------------------------------------------
# CORS 설정 (프론트엔드 허용 도메인)
# -----------------------------------------------------------------------------
# 콤마로 구분하여 여러 도메인 허용
# 예: https://your-frontend.com,https://www.your-frontend.com
CORS_ALLOWED_ORIGINS=https://skax.co.kr,https://www.skax.co.kr

# -----------------------------------------------------------------------------
# Oracle Cloud Object Storage (S3 호환)
# -----------------------------------------------------------------------------
S3_BUCKET_NAME=skax-reports-prod
AWS_REGION=ap-northeast-2
AWS_ACCESS_KEY=your-oracle-oci-access-key
AWS_SECRET_KEY=your-oracle-oci-secret-key

# -----------------------------------------------------------------------------
# Mail (SendGrid)
# -----------------------------------------------------------------------------
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-production-sendgrid-key

# -----------------------------------------------------------------------------
# 외부 API
# -----------------------------------------------------------------------------
KAKAO_API_KEY=your-production-kakao-key
```

**저장 방법** (vi 에디터):
1. `i` 키를 눌러 INSERT 모드 진입
2. 위 내용 붙여넣기
3. `ESC` 키를 눌러 명령 모드로
4. `:wq` 입력 후 엔터 (저장 및 종료)

### Step 5: 포트 방화벽 설정

#### 서버 내부 방화벽 (iptables/firewalld)

```bash
# firewalld 사용 시
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload

# 또는 iptables 사용 시
sudo iptables -I INPUT -p tcp --dport 8080 -j ACCEPT
sudo service iptables save

# 방화벽 규칙 확인
sudo firewall-cmd --list-all
# 또는
sudo iptables -L -n
```

#### Oracle Cloud Console 보안 규칙

1. **OCI Console 접속**: https://cloud.oracle.com
2. **Networking** > **Virtual Cloud Networks** 선택
3. 해당 VCN 클릭
4. **Security Lists** 클릭
5. Default Security List 선택
6. **Add Ingress Rules** 클릭
7. 다음 정보 입력:
   ```
   Source CIDR: 0.0.0.0/0
   IP Protocol: TCP
   Destination Port Range: 8080
   Description: Spring Boot Application
   ```
8. **Add Ingress Rules** 버튼 클릭

---

## 3. 수동 배포

수동 배포는 SSH로 서버에 접속하여 직접 배포 스크립트를 실행하는 방식입니다.

### 배포 절차

```bash
# 1. 서버 접속
ssh -i ~/.ssh/oracle-key.pem opc@your-server-ip

# 2. 프로젝트 디렉토리 이동
cd /home/opc/backend_team_java

# 3. 최신 코드 가져오기
git pull origin main

# 4. 배포 스크립트 실행 권한 부여 (최초 1회)
chmod +x ./docker-deploy.sh

# 5. 배포 실행 (이미지 빌드 + 컨테이너 실행)
./docker-deploy.sh deploy

# 6. 배포 확인
./docker-deploy.sh status
```

### 배포 스크립트 명령어

```bash
# 전체 배포 (빌드 + 중지 + 실행)
./docker-deploy.sh deploy

# 이미지만 빌드
./docker-deploy.sh build

# 컨테이너 중지 및 삭제
./docker-deploy.sh stop

# 컨테이너 실행
./docker-deploy.sh run

# 로그 확인 (실시간)
./docker-deploy.sh logs

# 상태 확인
./docker-deploy.sh status

# 미사용 이미지 정리
./docker-deploy.sh cleanup
```

### Health Check

```bash
# 로컬에서 확인 (서버 내부)
curl http://localhost:8080/actuator/health

# 외부에서 확인 (로컬 PC)
curl http://your-server-ip:8080/actuator/health

# 예상 응답
{"status":"UP"}
```

### API 테스트

```bash
# Swagger UI 접속
http://your-server-ip:8080/swagger-ui.html

# Health 엔드포인트
curl http://your-server-ip:8080/api/health
```

---

## 4. GitHub Actions 자동 배포

GitHub Actions를 사용하면 `main` 브랜치에 push할 때 자동으로 배포됩니다.

### 배포 워크플로우

```
1. 개발자가 main 브랜치에 push
   ↓
2. CI 워크플로우 실행 (테스트, 빌드)
   - ci_java.yaml
   - docker-build.sh ci (GHCR에 이미지 푸시)
   ↓
3. CI 성공 시 CD 워크플로우 자동 트리거
   - cd_java.yaml
   ↓
4. CD 워크플로우가 Oracle 서버에 SSH 접속
   ↓
5. 서버에서 실행:
   - git pull origin main
   - ./docker-deploy.sh deploy
   ↓
6. 배포 완료
```

### GitHub Secrets 설정

GitHub Repository > Settings > Secrets and variables > Actions > New repository secret

**필수 Secrets:**

| Secret 이름 | 값 | 설명 |
|------------|-----|------|
| `SERVER_HOST` | `xxx.xxx.xxx.xxx` | Oracle 서버 IP 주소 |
| `SERVER_USERNAME` | `opc` | SSH 접속 사용자명 (기본값: opc) |
| `SERVER_SSH_KEY` | `-----BEGIN RSA PRIVATE KEY-----...` | SSH 개인키 전체 내용 |
| `SERVER_PORT` | `22` | SSH 포트 (기본값: 22) |
| `DEPLOY_PATH` | `/home/opc/backend_team_java` | 서버 내 프로젝트 경로 |

**SSH 키 복사 방법:**

```bash
# Windows
type C:\Users\YourName\.ssh\oracle-key.pem

# Linux / macOS
cat ~/.ssh/oracle-key.pem
```

전체 내용을 복사하여 `SERVER_SSH_KEY`에 붙여넣기

### 자동 배포 테스트

```bash
# 로컬에서 변경사항 commit 및 push
git add .
git commit -m "Test auto deployment"
git push origin main

# GitHub Actions 탭에서 워크플로우 실행 확인
# https://github.com/your-org/backend_team_java/actions
```

---

## 5. Oracle Cloud Object Storage 설정

Oracle Cloud Object Storage는 AWS S3 호환 API를 제공합니다.

### Step 1: 버킷 생성

1. **OCI Console 접속**: https://cloud.oracle.com
2. **Storage** > **Object Storage** > **Buckets** 선택
3. **Create Bucket** 클릭
4. 다음 정보 입력:
   ```
   Bucket Name: skax-reports-prod
   Storage Tier: Standard
   Encryption: Encrypt using Oracle-managed keys
   ```
5. **Public Access** 비활성화 (중요!)
6. **Create** 버튼 클릭

### Step 2: API 키 생성

1. **OCI Console** > **Identity** > **Users** 선택
2. 본인 사용자 클릭
3. **API Keys** 섹션에서 **Add API Key** 클릭
4. **Generate API Key Pair** 선택
5. **Download Private Key** 클릭하여 저장
6. **Add** 버튼 클릭
7. Configuration File Preview에서 정보 확인:
   ```
   [DEFAULT]
   user=ocid1.user.oc1..xxxxx
   fingerprint=xx:xx:xx:xx:xx
   tenancy=ocid1.tenancy.oc1..xxxxx
   region=ap-seoul-1
   key_file=~/.oci/oci_api_key.pem
   ```

### Step 3: S3 호환 엔드포인트 설정

`.env` 파일에 다음 정보 추가:

```bash
# Oracle Cloud Object Storage
S3_BUCKET_NAME=skax-reports-prod
AWS_REGION=ap-northeast-2
AWS_ACCESS_KEY=your-oci-access-key
AWS_SECRET_KEY=your-oci-secret-key
ORACLE_NAMESPACE=your-namespace
ORACLE_REGION=ap-seoul-1
```

**Namespace 확인 방법:**
- OCI Console > **Tenancy Details** > **Object Storage Namespace**

### Step 4: application-prod.yml 설정

프로젝트의 `src/main/resources/application-prod.yml`에 다음 추가:

```yaml
aws:
  s3:
    bucket-name: ${S3_BUCKET_NAME}
    region: ${AWS_REGION}
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}
    endpoint: https://${ORACLE_NAMESPACE}.compat.objectstorage.${ORACLE_REGION}.oraclecloud.com
```

---

## 6. 서버 모니터링

### 컨테이너 상태 확인

```bash
# SSH 접속
ssh -i ~/.ssh/oracle-key.pem opc@your-server-ip

# 실행 중인 컨테이너 확인
docker ps

# 컨테이너 상세 정보
docker inspect backend-springboot

# 로그 확인 (실시간)
docker logs -f backend-springboot

# 최근 100줄만 확인
docker logs --tail 100 backend-springboot

# 리소스 사용량 확인
docker stats backend-springboot
```

### 서버 리소스 모니터링

```bash
# CPU, 메모리 사용량 확인
top

# htop 설치 및 사용 (더 편리)
sudo yum install -y htop
htop

# 디스크 사용량 확인
df -h

# 메모리 사용량 상세 확인
free -h

# 네트워크 포트 확인
netstat -tlnp | grep 8080
ss -tlnp | grep 8080

# 프로세스 목록
ps aux | grep java
```

### 로그 파일 관리

```bash
# Docker 로그를 파일로 저장
docker logs backend-springboot > /var/log/backend-app.log 2>&1

# 로그 파일 실시간 확인
tail -f /var/log/backend-app.log

# 로그 파일 크기 확인
du -h /var/log/backend-app.log

# 로그 로테이션 설정
sudo vi /etc/logrotate.d/backend-app
```

**로그 로테이션 설정 예시** (`/etc/logrotate.d/backend-app`):

```
/var/log/backend-app.log {
    daily
    rotate 7
    compress
    missingok
    notifempty
    create 0644 opc opc
}
```

### 알림 설정 (선택사항)

OCI Console에서 Monitoring 및 Notifications 설정 가능:
- CPU 사용률 80% 이상
- 메모리 사용률 90% 이상
- 디스크 사용률 85% 이상

---

## 7. 장애 대응 및 롤백

### 컨테이너 재시작

```bash
# 컨테이너 재시작 (설정 변경 없음)
docker restart backend-springboot

# 또는 전체 재배포 (이미지 재빌드)
cd /home/opc/backend_team_java
./docker-deploy.sh deploy
```

### 롤백 (이전 버전으로)

#### 방법 1: 이전 커밋으로 롤백

```bash
cd /home/opc/backend_team_java

# 커밋 이력 확인
git log --oneline -10

# 출력 예시:
# abc1234 Fix bug in auth service
# def5678 Add new feature
# ghi9012 Update dependencies

# 이전 커밋으로 롤백
git checkout def5678

# 재배포
./docker-deploy.sh deploy

# 롤백 확인
./docker-deploy.sh status
curl http://localhost:8080/actuator/health
```

#### 방법 2: Git 태그로 롤백

```bash
# 태그 목록 확인
git tag

# 출력 예시:
# v1.0.0
# v1.0.1
# v1.1.0

# 특정 버전으로 롤백
git checkout v1.0.1

# 재배포
./docker-deploy.sh deploy
```

#### 롤백 후 다시 최신 버전으로

```bash
# main 브랜치로 되돌리기
git checkout main
git pull origin main

# 재배포
./docker-deploy.sh deploy
```

### 데이터베이스 연결 문제

```bash
# DB 연결 테스트 (외부에서)
telnet your-db-host 5432
nc -zv your-db-host 5432

# 컨테이너 내에서 DB 연결 테스트
docker exec -it backend-springboot sh

# 컨테이너 내부에서
wget -qO- your-db-host:5432 || echo "Connection failed"
exit
```

### 긴급 대응 체크리스트

- [ ] 로그 확인: `docker logs backend-springboot`
- [ ] 컨테이너 상태 확인: `docker ps -a`
- [ ] 서버 리소스 확인: `top`, `free -h`, `df -h`
- [ ] 환경 변수 확인: `cat .env`
- [ ] 네트워크 확인: `netstat -tlnp | grep 8080`
- [ ] 방화벽 확인: `sudo firewall-cmd --list-all`
- [ ] DB 연결 확인
- [ ] 필요 시 롤백 실행

---

## 8. 보안 강화

### SSH 보안

```bash
# 1. SSH 키 권한 설정 (로컬 PC)
chmod 600 ~/.ssh/oracle-key.pem

# 2. SSH 설정 강화 (서버)
sudo vi /etc/ssh/sshd_config

# 다음 설정 확인:
# PasswordAuthentication no
# PubkeyAuthentication yes
# PermitRootLogin no

# SSH 재시작
sudo systemctl restart sshd
```

### 방화벽 최소화

```bash
# 필요한 포트만 열기
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload

# 불필요한 포트 차단
sudo firewall-cmd --permanent --remove-port=8080/tcp  # 외부 접근 불필요시
sudo firewall-cmd --reload
```

### 정기 업데이트

```bash
# 시스템 패키지 업데이트
sudo yum update -y

# Docker 업데이트
sudo yum update docker-engine -y
sudo systemctl restart docker
```

### Docker 이미지 보안

```bash
# 이미지 취약점 스캔
docker scan backend-springboot:latest

# 또는 Trivy 사용
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image backend-springboot:latest
```

### 환경 변수 보안

```bash
# .env 파일 권한 설정
chmod 600 .env

# .env 파일 소유권 확인
ls -l .env
# -rw------- 1 opc opc ... .env

# Git에서 제외 확인
cat .gitignore | grep .env
# .env 항목이 있어야 함
```

### 정기 백업

```bash
# DB 백업 스크립트 예시
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/home/opc/backups"

mkdir -p $BACKUP_DIR

# PostgreSQL 백업
PGPASSWORD=$DB_PASSWORD pg_dump -h $DB_HOST -U $DB_USERNAME -d $DB_NAME \
  > $BACKUP_DIR/db_backup_$DATE.sql

# 오래된 백업 삭제 (30일 이상)
find $BACKUP_DIR -name "db_backup_*.sql" -mtime +30 -delete

echo "Backup completed: db_backup_$DATE.sql"
```

---

## 부록

### 유용한 명령어

```bash
# 서버 재부팅
sudo reboot

# 디스크 정리
docker system prune -a -f
sudo yum clean all

# 로그 파일 정리
sudo journalctl --vacuum-time=7d

# Docker 서비스 상태 확인
sudo systemctl status docker

# Docker 서비스 재시작
sudo systemctl restart docker
```

### 트러블슈팅

| 문제 | 해결 방법 |
|-----|---------|
| 포트 8080이 이미 사용 중 | `sudo lsof -i :8080`로 확인 후 프로세스 종료 |
| Docker 빌드 실패 | `docker system prune -a`로 정리 후 재시도 |
| 메모리 부족 | `free -h`로 확인, 컨테이너 재시작 |
| DB 연결 실패 | 방화벽 및 Security List 확인 |
| Git pull 실패 | `git reset --hard origin/main`으로 초기화 |

---

## 참고 자료

- [Oracle Cloud Infrastructure 문서](https://docs.oracle.com/en-us/iaas/Content/home.htm)
- [Oracle Cloud Object Storage S3 호환 API](https://docs.oracle.com/en-us/iaas/Content/Object/Tasks/s3compatibleapi.htm)
- [Docker 공식 문서](https://docs.docker.com/)
- [Spring Boot Production 가이드](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)
- [로컬 CI/CD 테스트 가이드](LOCAL_CICD_TEST_GUIDE.md)

---

## 문의 및 지원

문제가 발생하면:

1. 로그 확인: `docker logs backend-springboot`
2. 서버 리소스 확인: `top`, `free -h`, `df -h`
3. GitHub Issues 등록
4. 팀 개발자에게 문의

---

**작성일**: 2025-11-24
**버전**: 1.0.0
**작성자**: SKAX Team
