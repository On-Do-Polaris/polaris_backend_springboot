# GitHub Secrets 환경변수 설정 가이드

> SKALA Physical Risk AI - Backend SpringBoot
>
> 최종 수정일: 2025-11-24
> 버전: v01

---

## 📋 목차

1. [GitHub Secrets란?](#github-secrets란)
2. [Secrets 등록 방법](#secrets-등록-방법)
3. [필수 환경변수 목록](#필수-환경변수-목록)
4. [환경변수 상세 설명](#환경변수-상세-설명)
5. [보안 권장사항](#보안-권장사항)
6. [검증 방법](#검증-방법)

---

## GitHub Secrets란?

GitHub Secrets는 GitHub Actions 워크플로우에서 사용하는 민감한 정보(API 키, 비밀번호, 토큰 등)를 안전하게 저장하는 기능입니다.

**특징:**
- 암호화되어 저장
- GitHub Actions 실행 중에만 접근 가능
- 로그에 자동으로 마스킹 처리
- Repository, Environment, Organization 레벨에서 관리 가능

---

## Secrets 등록 방법

### 1. GitHub Repository 접속

1. GitHub에서 해당 Repository로 이동
2. **Settings** 탭 클릭
3. 왼쪽 메뉴에서 **Secrets and variables** > **Actions** 클릭

### 2. Secret 추가

1. **New repository secret** 버튼 클릭
2. **Name**: Secret 이름 입력 (대문자와 언더스코어만 사용)
3. **Secret**: 실제 값 입력
4. **Add secret** 버튼 클릭

### 3. 환경별 Secret (Optional)

프로덕션/스테이징 환경을 분리하려면:
1. **Environments** 메뉴 클릭
2. 환경 생성 (예: `production`, `staging`)
3. 각 환경에 Secret 추가

---

## 필수 환경변수 목록

### ⭐ CI/CD 워크플로우용 (9개)

| Secret 이름 | 필수 | 사용 위치 | 설명 |
|------------|------|----------|------|
| `JWT_SECRET` | ✅ | CI (테스트) | JWT 토큰 서명 키 |
| `FASTAPI_API_KEY` | ✅ | CI (테스트) | FastAPI 인증 키 |
| `SERVER_HOST` | ✅ | CD (배포) | Oracle 서버 IP |
| `SERVER_USERNAME` | ✅ | CD (배포) | SSH 접속 유저명 |
| `SERVER_SSH_KEY` | ✅ | CD (배포) | SSH Private Key |
| `SERVER_PORT` | ⚪ | CD (배포) | SSH 포트 (기본값: 22) |
| `DEPLOY_PATH` | ✅ | CD (배포) | 배포 경로 |
| `GITHUB_TOKEN` | 🔵 | CI (빌드) | 자동 제공됨 (설정 불필요) |

> **범례**
> - ✅ 필수
> - ⚪ 선택 (기본값 있음)
> - 🔵 자동 제공

### 🌐 서버 환경변수 (.env 파일용, 13개)

서버의 `.env` 파일에 직접 설정해야 하는 환경변수입니다.

| 변수 이름 | 필수 | 설명 |
|----------|------|------|
| `SERVER_PORT` | ⚪ | Spring Boot 서버 포트 (기본값: 8080) |
| `SPRING_PROFILES_ACTIVE` | ✅ | Spring 프로파일 (local, staging, prod) |
| `DB_HOST` | ✅ | PostgreSQL 호스트 |
| `DB_PORT` | ✅ | PostgreSQL 포트 (기본값: 5432) |
| `DB_USERNAME` | ✅ | DB 유저명 |
| `DB_PASSWORD` | ✅ | DB 비밀번호 |
| `REDIS_HOST` | ✅ | Redis 호스트 |
| `REDIS_PORT` | ✅ | Redis 포트 (기본값: 6379) |
| `REDIS_PASSWORD` | ⚪ | Redis 비밀번호 |
| `JWT_SECRET` | ✅ | JWT 서명 키 (256비트 이상) |
| `FASTAPI_BASE_URL` | ✅ | FastAPI AI Agent 서버 URL |
| `FASTAPI_API_KEY` | ✅ | FastAPI 인증 키 |
| `CORS_ALLOWED_ORIGINS` | ✅ | 프론트엔드 허용 도메인 (콤마 구분) |
| `AWS_ACCESS_KEY` | ✅ | Oracle Cloud Object Storage Access Key |
| `AWS_SECRET_KEY` | ✅ | Oracle Cloud Object Storage Secret Key |
| `S3_BUCKET_NAME` | ✅ | Object Storage 버킷 이름 |
| `AWS_REGION` | ✅ | 리전 (예: ap-northeast-2) |
| `MAIL_HOST` | ⚪ | 메일 서버 호스트 |
| `MAIL_PORT` | ⚪ | 메일 서버 포트 |
| `MAIL_USERNAME` | ⚪ | 메일 유저명 |
| `MAIL_PASSWORD` | ⚪ | 메일 비밀번호 |
| `KAKAO_API_KEY` | ⚪ | 카카오 REST API 키 |

---

## 환경변수 상세 설명

### 1. CI 워크플로우 환경변수

#### `JWT_SECRET` ✅
**용도**: JWT 액세스/리프레시 토큰 서명 및 검증

**생성 방법**:
```bash
# OpenSSL로 안전한 256비트 랜덤 키 생성
openssl rand -base64 64

# 또는 온라인 생성기 사용
# https://generate-random.org/api-key-generator
```

**예시**:
```
skax-physical-risk-management-production-jwt-secret-2024-must-be-at-least-256-bits-long-for-hs256-algorithm-random-key-here
```

**주의사항**:
- 최소 256비트 (64자 이상) 필요
- 절대로 GitHub 코드에 커밋하지 말 것
- 프로덕션과 개발 환경에서 다른 키 사용

---

#### `FASTAPI_API_KEY` ✅
**용도**: FastAPI AI Agent 서버와의 통신 인증

**생성 방법**:
```bash
# UUID v4 생성
python3 -c "import uuid; print(str(uuid.uuid4()))"

# 또는
uuidgen
```

**예시**:
```
a1b2c3d4-e5f6-7890-1234-567890abcdef
```

**주의사항**:
- FastAPI 서버의 `.env` 파일에도 동일한 키 설정 필요
- 키가 일치하지 않으면 AI 분석 요청 실패

---

#### `CORS_ALLOWED_ORIGINS` ✅
**용도**: 프론트엔드 도메인 허용 목록 (CORS 정책)

**형식**:
```bash
# 콤마로 구분하여 여러 도메인 설정
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,https://your-frontend-domain.com
```

**예시 (개발 환경)**:
```
http://localhost:3000,http://localhost:5173,http://localhost:8080
```

**예시 (프로덕션)**:
```
https://skax.co.kr,https://www.skax.co.kr,https://app.skax.co.kr
```

**주의사항**:
- 각 URL 사이에 공백 없이 콤마로 구분
- 프로토콜(http/https) 포함 필수
- 마지막에 슬래시(/) 붙이지 않기 (❌ `http://localhost:3000/`)

---

#### `FASTAPI_BASE_URL` ✅
**용도**: FastAPI AI Agent 서버 연결 URL

**형식**:
```bash
FASTAPI_BASE_URL=http://fastapi-server:8000
```

**예시 (개발 환경)**:
```
http://localhost:8000
```

**예시 (프로덕션 - 같은 서버)**:
```
http://localhost:8000
```

**예시 (프로덕션 - 다른 서버)**:
```
http://192.168.1.100:8000
https://ai.skax.co.kr
```

---

#### `SERVER_PORT` ⚪
**용도**: Spring Boot 애플리케이션 포트

**기본값**: `8080`

**형식**:
```bash
SERVER_PORT=8080
```

**주의사항**:
- 다른 서비스와 포트 충돌 확인 필요
- 방화벽에서 해당 포트 열어야 함

---

### 2. CD 워크플로우 환경변수

#### `SERVER_HOST` ✅
**용도**: Oracle Cloud 서버 IP 주소 또는 도메인

**예시**:
```
# Public IP
132.226.15.123

# 또는 도메인
api.skala-physical-risk.com
```

**확인 방법**:
```bash
# Oracle Cloud Console에서 확인
# Compute > Instances > 인스턴스 선택 > Public IP
```

---

#### `SERVER_USERNAME` ✅
**용도**: SSH 접속 시 사용할 유저명

**예시**:
```
ubuntu
```

**기본값**:
- Ubuntu: `ubuntu`
- Oracle Linux: `opc`
- CentOS: `centos`

---

#### `SERVER_SSH_KEY` ✅
**용도**: SSH Private Key (비밀키)

**생성 및 등록 방법**:

**1. SSH 키페어 생성** (아직 없는 경우)
```bash
# 로컬 PC에서 실행
ssh-keygen -t ed25519 -C "github-actions-deploy"

# 저장 위치: ~/.ssh/id_ed25519 (기본값)
# Passphrase: 입력하지 않음 (GitHub Actions에서 사용 시)
```

**2. Public Key를 서버에 등록**
```bash
# Public Key 내용 복사
cat ~/.ssh/id_ed25519.pub

# 서버에 접속하여 등록
ssh ubuntu@SERVER_IP
mkdir -p ~/.ssh
chmod 700 ~/.ssh
echo "복사한_public_key_내용" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

**3. Private Key를 GitHub Secret에 등록**
```bash
# Private Key 전체 내용 복사 (BEGIN ~ END 포함)
cat ~/.ssh/id_ed25519
```

**Private Key 예시**:
```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
QyNTUxOQAAACDGc7VWLhxF8xPYXqPzN7Zk4EZvK3bKJ8H2FqX3mHVWUQAAAJg3Q8YrN0PG
KwAAAAtzc2gtZWQyNTUxOQAAACDGc7VWLhxF8xPYXqPzN7Zk4EZvK3bKJ8H2FqX3mHVWUQ
...
-----END OPENSSH PRIVATE KEY-----
```

**주의사항**:
- **BEGIN**부터 **END**까지 전체 내용 복사
- 줄바꿈 포함하여 그대로 복사
- Private Key는 절대 공개하지 말 것

---

#### `SERVER_PORT` ⚪
**용도**: SSH 접속 포트

**기본값**: `22`

**예시**:
```
22
```

**사용자 정의 포트 사용 시**:
```
2222
```

---

#### `DEPLOY_PATH` ✅
**용도**: 서버에서 프로젝트가 위치한 절대 경로

**예시**:
```
/home/ubuntu/backend_team_java
```

**확인 방법**:
```bash
# 서버에서 실행
cd ~/backend_team_java
pwd
```

---

### 3. 서버 환경변수 (.env 파일)

서버의 `.env` 파일에 직접 설정합니다.

#### 데이터베이스 설정

```bash
# PostgreSQL (프로덕션)
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=strong_password_here_1234!@#$
```

**DB_PASSWORD 생성 예시**:
```bash
# 안전한 비밀번호 생성
openssl rand -base64 32
```

---

#### Redis 설정

```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_password_here_5678!@#$
```

**참고**: Redis 비밀번호 설정 방법
```bash
# Redis 설정 파일 편집
sudo vi /etc/redis/redis.conf

# 다음 줄 찾아서 수정
requirepass redis_password_here_5678!@#$

# Redis 재시작
sudo systemctl restart redis
```

---

#### Oracle Cloud Object Storage 설정

```bash
# Oracle Cloud Object Storage (S3 호환 API)
AWS_REGION=ap-northeast-2
S3_BUCKET_NAME=skax-reports
AWS_ACCESS_KEY=your_oracle_access_key_here
AWS_SECRET_KEY=your_oracle_secret_key_here
```

**Access Key/Secret Key 생성 방법**:

1. **Oracle Cloud Console 접속**
   - https://cloud.oracle.com

2. **Customer Secret Keys 생성**
   - Profile Icon 클릭 > **User Settings**
   - **Resources** > **Customer Secret Keys**
   - **Generate Secret Key** 클릭
   - Key Name: `skax-backend-api`
   - **Secret Key 복사** (한 번만 표시됨!)
   - **Access Key** 확인 (생성된 키 목록에서)

3. **버킷 생성**
   - **Storage** > **Object Storage & Archive Storage** > **Buckets**
   - **Create Bucket** 클릭
   - Bucket Name: `skax-reports`
   - Storage Tier: **Standard**
   - Visibility: **Private**

---

#### FastAPI 설정

```bash
FASTAPI_BASE_URL=http://fastapi-server:8000
FASTAPI_API_KEY=a1b2c3d4-e5f6-7890-1234-567890abcdef
```

**주의**: `FASTAPI_API_KEY`는 CI 워크플로우의 Secret과 동일해야 함

---

#### 메일 설정 (선택)

```bash
# SendGrid 사용 시
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=SG.your_sendgrid_api_key_here

# 또는 Gmail 사용 시
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-specific-password
```

**SendGrid API Key 발급**:
1. https://sendgrid.com 회원가입
2. **Settings** > **API Keys**
3. **Create API Key**
4. Full Access 권한 부여

**Gmail App Password 발급**:
1. Google 계정 > **보안**
2. **2단계 인증** 활성화
3. **앱 비밀번호** 생성
4. 앱 선택: **메일**, 기기 선택: **기타**

---

#### 카카오 API (선택)

```bash
KAKAO_API_KEY=your_kakao_rest_api_key_here
```

**Kakao REST API Key 발급**:
1. https://developers.kakao.com 접속
2. 내 애플리케이션 > **애플리케이션 추가하기**
3. **앱 설정** > **앱 키** > **REST API 키** 복사

---

## 보안 권장사항

### 1. Secrets 관리

✅ **해야 할 것**:
- 프로덕션과 개발 환경에서 다른 키 사용
- 정기적으로 키 로테이션 (최소 6개월마다)
- 최소 권한 원칙 적용
- Secret 값은 절대 로그에 출력하지 않기

❌ **하지 말아야 할 것**:
- GitHub 코드에 Secret 하드코딩
- Slack, 이메일 등으로 Secret 공유
- Public Repository에 Private Key 커밋
- 테스트/개발용 키를 프로덕션에 사용

---

### 2. SSH Key 보안

```bash
# Private Key 파일 권한 설정 (로컬)
chmod 600 ~/.ssh/id_ed25519

# Public Key는 공개해도 안전함
chmod 644 ~/.ssh/id_ed25519.pub

# 서버의 authorized_keys 권한
chmod 600 ~/.ssh/authorized_keys
chmod 700 ~/.ssh
```

---

### 3. 비밀번호 복잡도

강력한 비밀번호 규칙:
- 최소 16자 이상
- 대문자, 소문자, 숫자, 특수문자 조합
- 사전 단어 사용 금지
- 주기적으로 변경 (6개월)

**좋은 예**:
```
Sk@x2024#ProdDB_P@ssw0rd!Random$Key
```

**나쁜 예**:
```
password123
admin
skax2024
```

---

### 4. .env 파일 보호

```bash
# 서버에서 .env 파일 권한 설정
chmod 600 .env
chown ubuntu:ubuntu .env

# .gitignore에 추가 (이미 되어있음)
echo ".env" >> .gitignore
```

---

## 검증 방법

### 1. GitHub Secrets 설정 확인

```bash
# GitHub Actions 워크플로우 수동 실행
# Repository > Actions > CI - Test & Build > Run workflow
```

**확인 항목**:
- [ ] CI 워크플로우가 성공적으로 완료되는가?
- [ ] 테스트가 통과하는가?
- [ ] Docker 이미지가 정상적으로 빌드되는가?

---

### 2. CD 배포 테스트

```bash
# main 브랜치에 Push하여 자동 배포 트리거
git push origin main
```

**확인 항목**:
- [ ] CD 워크플로우가 트리거되는가?
- [ ] SSH 접속이 성공하는가?
- [ ] 배포 스크립트가 정상 실행되는가?
- [ ] 서버에서 컨테이너가 실행되는가?

---

### 3. 서버 환경변수 테스트

```bash
# 서버 접속
ssh ubuntu@SERVER_IP

# 프로젝트 디렉토리 이동
cd ~/backend_team_java

# .env 파일 확인 (비밀번호는 표시 안 됨)
cat .env

# 애플리케이션 로그 확인
docker logs backend-springboot

# 정상 작동 확인
curl http://localhost:8080/actuator/health
```

**응답 예시**:
```json
{
  "status": "UP"
}
```

---

### 4. 환경변수 누락 확인

**증상별 해결 방법**:

| 증상 | 원인 | 해결 |
|-----|------|------|
| CI 테스트 실패 | `JWT_SECRET` 또는 `FASTAPI_API_KEY` 누락 | GitHub Secrets 재확인 |
| CD 배포 실패 | `SERVER_*` 관련 Secret 누락 | SSH 설정 재확인 |
| 앱 시작 실패 | 서버 `.env` 파일 누락 | `.env` 파일 생성 및 권한 설정 |
| DB 연결 실패 | `DB_*` 환경변수 오류 | PostgreSQL 실행 및 비밀번호 확인 |
| S3 업로드 실패 | `AWS_*` 환경변수 오류 | Object Storage 설정 확인 |

---

## 빠른 체크리스트

### GitHub Secrets (필수 9개)

- [ ] `JWT_SECRET` (64자 이상 랜덤 문자열)
- [ ] `FASTAPI_API_KEY` (UUID v4)
- [ ] `SERVER_HOST` (Oracle 서버 IP)
- [ ] `SERVER_USERNAME` (SSH 유저명)
- [ ] `SERVER_SSH_KEY` (Private Key 전체)
- [ ] `SERVER_PORT` (기본값 22)
- [ ] `DEPLOY_PATH` (프로젝트 절대 경로)

### 서버 .env 파일 (필수 13개)

- [ ] `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`
- [ ] `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- [ ] `JWT_SECRET` (GitHub Secret과 동일)
- [ ] `FASTAPI_BASE_URL`, `FASTAPI_API_KEY`
- [ ] `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `S3_BUCKET_NAME`, `AWS_REGION`

### 선택 (4개)

- [ ] `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- [ ] `KAKAO_API_KEY`

---

## 문제 해결

### Q1. GitHub Actions에서 "Secret not found" 에러

**원인**: Secret 이름 오타 또는 등록되지 않음

**해결**:
```bash
# 워크플로우 파일에서 사용하는 이름과 정확히 일치하는지 확인
# 대소문자 구분 필수
${{ secrets.JWT_SECRET }}  # ✅
${{ secrets.jwt_secret }}  # ❌
```

---

### Q2. SSH 접속 실패 (Permission denied)

**원인**: Private Key가 잘못되었거나 서버에 Public Key가 등록되지 않음

**해결**:
```bash
# 1. 로컬에서 SSH 접속 테스트
ssh -i ~/.ssh/id_ed25519 ubuntu@SERVER_IP

# 2. Public Key 재등록
ssh ubuntu@SERVER_IP
cat >> ~/.ssh/authorized_keys
# Public Key 붙여넣기 후 Ctrl+D

# 3. 권한 재설정
chmod 600 ~/.ssh/authorized_keys
chmod 700 ~/.ssh
```

---

### Q3. 환경변수가 앱에서 인식 안 됨

**원인**: `.env` 파일 위치 또는 형식 오류

**해결**:
```bash
# 1. .env 파일이 프로젝트 루트에 있는지 확인
ls -la ~/backend_team_java/.env

# 2. 형식 확인 (공백 없이, = 양쪽에)
# ✅ 올바른 형식
DB_HOST=localhost

# ❌ 잘못된 형식
DB_HOST = localhost  # 공백 있음
DB_HOST="localhost"  # 따옴표 불필요

# 3. 권한 확인
chmod 600 .env

# 4. 컨테이너 재시작
./docker-deploy.sh deploy
```

---

## 참고 자료

- [GitHub Secrets 공식 문서](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [SSH Key 생성 가이드](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent)
- [Oracle Cloud Object Storage 문서](https://docs.oracle.com/en-us/iaas/Content/Object/Concepts/objectstorageoverview.htm)
- [SendGrid API 문서](https://docs.sendgrid.com/)

---

**문서 작성**: SKAX Physical Risk AI Team
**최종 수정**: 2025-11-24
**버전**: v01
