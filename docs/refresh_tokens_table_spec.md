# RefreshToken 테이블 생성 요청

## 요약

현재 리프레시 토큰을 메모리(ConcurrentHashMap)에 저장하고 있어, 서버 재시작 시 모든 토큰이 유실되는 문제가 있습니다.
DB에 `refresh_tokens` 테이블을 생성하여 토큰을 영구 저장하고, 보안 및 감사 기능을 강화하고자 합니다.

---

## 테이블 스키마

```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    device_info VARCHAR(255),
    ip_address VARCHAR(45),

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
```

---

## 인덱스 생성

```sql
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
```

**인덱스 목적:**
- `idx_refresh_tokens_user_id`: 사용자별 토큰 조회 성능 향상
- `idx_refresh_tokens_token`: 토큰 값으로 조회 시 성능 향상 (로그인 갱신)
- `idx_refresh_tokens_expires_at`: 만료된 토큰 일괄 삭제 시 성능 향상

---

## 컬럼 설명

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `id` | UUID | PRIMARY KEY | 토큰 레코드의 고유 식별자 |
| `user_id` | UUID | NOT NULL, FK | 사용자 테이블 외래키 (CASCADE 삭제) |
| `token` | VARCHAR(500) | NOT NULL, UNIQUE | JWT 리프레시 토큰 값 |
| `expires_at` | TIMESTAMP | NOT NULL | 토큰 만료 시간 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW | 토큰 생성 시간 |
| `revoked` | BOOLEAN | NOT NULL, DEFAULT FALSE | 토큰 무효화 여부 (로그아웃 시 true) |
| `device_info` | VARCHAR(255) | NULL | 선택사항: User-Agent 정보 |
| `ip_address` | VARCHAR(45) | NULL | 선택사항: 접속 IP 주소 (IPv4/IPv6) |

---

## 외래키 제약조건

```sql
CONSTRAINT fk_refresh_tokens_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
```

**ON DELETE CASCADE 이유:**
- 사용자 계정 삭제 시 관련된 모든 리프레시 토큰도 자동 삭제
- 데이터 정합성 유지

---

## 사용 시나리오

### 1. 로그인 시
```sql
INSERT INTO refresh_tokens (user_id, token, expires_at, device_info, ip_address)
VALUES (
    'user-uuid-here',
    'jwt-refresh-token-value',
    NOW() + INTERVAL '7 days',
    'Mozilla/5.0...',
    '192.168.1.100'
);
```

### 2. 토큰 갱신 시
```sql
-- 기존 토큰 조회 및 검증
SELECT * FROM refresh_tokens
WHERE token = 'jwt-refresh-token-value'
  AND revoked = false
  AND expires_at > NOW();

-- 기존 토큰 무효화
UPDATE refresh_tokens
SET revoked = true
WHERE token = 'jwt-refresh-token-value';

-- 새 토큰 생성
INSERT INTO refresh_tokens (user_id, token, expires_at)
VALUES ('user-uuid', 'new-jwt-token', NOW() + INTERVAL '7 days');
```

### 3. 로그아웃 시
```sql
-- 해당 사용자의 모든 토큰 무효화
UPDATE refresh_tokens
SET revoked = true
WHERE user_id = 'user-uuid';
```

### 4. 만료된 토큰 정리 (일일 배치)
```sql
DELETE FROM refresh_tokens
WHERE expires_at < NOW();
```

---

## 예상 데이터 규모

- **일일 로그인 수**: 약 1,000건 (가정)
- **토큰 유효기간**: 7일
- **예상 레코드 수**: 약 7,000건 (최대)
- **일일 삭제**: 약 1,000건 (만료 토큰)

---

## 보안 고려사항

1. **토큰 값 암호화**: 애플리케이션 레벨에서 JWT로 서명됨
2. **만료 시간 강제**: `expires_at` 컬럼으로 만료 관리
3. **Revoke 기능**: `revoked` 컬럼으로 즉시 무효화 가능
4. **CASCADE 삭제**: 사용자 삭제 시 토큰 자동 삭제
5. **감사 로그**: `device_info`, `ip_address`로 추적 가능

---

## ERD 추가 내용

ERD 문서 ([docs/erd.md](erd.md))의 **2.2 User Tables** 섹션에 다음 내용 추가:

```markdown
#### refresh_tokens - 리프레시 토큰

**필요 이유:** 로그인 세션 유지를 위한 리프레시 토큰 관리

**사용처:**
- SpringBoot: AuthService (로그인, 로그아웃, 토큰 갱신)
- 보안: 멀티 디바이스 로그인 추적 및 관리

| 컬럼명 | 타입 | 설명 | 역할 |
|--------|------|------|------|
| id | UUID PK | 토큰 ID | 내부 식별자 |
| user_id | UUID FK | 사용자 ID | users 참조 (CASCADE) |
| token | VARCHAR(500) UK | JWT 토큰값 | 토큰 조회 키 |
| expires_at | TIMESTAMP | 만료 시간 | 토큰 유효성 검증 |
| created_at | TIMESTAMP | 생성 시간 | 감사 로그 |
| revoked | BOOLEAN | 무효화 여부 | 로그아웃/보안 |
| device_info | VARCHAR(255) | 기기 정보 | User-Agent |
| ip_address | VARCHAR(45) | IP 주소 | 접속 추적 |
```

---

## 관련 백엔드 작업

백엔드 팀에서는 다음 작업을 수행합니다:

1. **RefreshToken JPA 엔티티 생성**
2. **RefreshTokenRepository 인터페이스 생성**
3. **AuthService 수정** (메모리 저장소 → DB 저장소)
4. **토큰 정리 스케줄러 구현** (매일 새벽 3시 실행)

---

**작성일**: 2025-12-03
**작성자**: Backend Team
**검토자**: DB Team
