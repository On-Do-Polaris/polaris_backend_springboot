# 데이터베이스 조회 및 저장 로직 정리

Spring Boot 백엔드 API의 데이터베이스 조회 및 저장 로직을 Repository와 Service 계층으로 구분하여 정리한 문서입니다.

## 목차
- [Repository 계층](#repository-계층)
- [Service 계층](#service-계층)
- [트랜잭션 관리](#트랜잭션-관리)

---

## Repository 계층

JPA Repository를 통한 데이터베이스 접근 계층입니다. Spring Data JPA를 사용하여 기본 CRUD 및 커스텀 쿼리를 제공합니다.

### 1. UserRepository

**위치**: [domain/user/repository/UserRepository.java](src/main/java/com/skax/physicalrisk/domain/user/repository/UserRepository.java)

**엔티티**: User

**조회 메서드**:
- `Optional<User> findByEmail(String email)` - 이메일로 사용자 조회
- `boolean existsByEmail(String email)` - 이메일 중복 확인

**사용 위치**:
- AuthService: 회원가입, 로그인, 비밀번호 재설정
- UserService: 사용자 정보 조회/수정/삭제
- SiteService: 사업장 권한 확인
- AnalysisService: 분석 권한 확인
- ReportService: 리포트 권한 확인

---

### 2. RefreshTokenRepository

**위치**: [domain/user/repository/RefreshTokenRepository.java](src/main/java/com/skax/physicalrisk/domain/user/repository/RefreshTokenRepository.java)

**엔티티**: RefreshToken

**조회 메서드**:
- `Optional<RefreshToken> findByToken(String token)` - 토큰 값으로 조회
- `List<RefreshToken> findByUser(User user)` - 사용자의 모든 토큰 조회

**저장/수정 메서드**:
- `save(RefreshToken)` - 토큰 저장 (JPA 기본 메서드)

**삭제 메서드**:
- `@Modifying void revokeAllByUser(@Param("user") User user)` - 사용자의 모든 토큰 폐기 (UPDATE 쿼리)
- `@Modifying int deleteExpiredTokens(@Param("now") LocalDateTime now)` - 만료된 토큰 삭제
- `@Modifying int deleteRevokedTokens()` - 폐기된 토큰 삭제

**사용 위치**:
- AuthService: 로그인, 로그아웃, 토큰 갱신
- TokenCleanupScheduler: 주기적 토큰 정리

---

### 3. PasswordResetTokenRepository

**위치**: [domain/user/repository/PasswordResetTokenRepository.java](src/main/java/com/skax/physicalrisk/domain/user/repository/PasswordResetTokenRepository.java)

**엔티티**: PasswordResetToken

**조회 메서드**:
- `Optional<PasswordResetToken> findByToken(String token)` - 토큰으로 조회
- `Optional<PasswordResetToken> findByUserAndUsedFalseAndExpiresAtAfter(User user, LocalDateTime now)` - 사용자의 유효한 미사용 토큰 조회

**저장 메서드**:
- `save(PasswordResetToken)` - 토큰 저장 (JPA 기본 메서드)

**삭제 메서드**:
- `void deleteByExpiresAtBefore(LocalDateTime now)` - 만료된 토큰 삭제

**사용 위치**:
- AuthService: 비밀번호 재설정 요청/확인

---

### 4. SiteRepository

**위치**: [domain/site/repository/SiteRepository.java](src/main/java/com/skax/physicalrisk/domain/site/repository/SiteRepository.java)

**엔티티**: Site

**조회 메서드**:
- `List<Site> findByUser(User user)` - 사용자의 전체 사업장 목록
- `Page<Site> findByUser(User user, Pageable pageable)` - 사용자의 사업장 목록 (페이징)
- `Optional<Site> findByIdAndUser(UUID id, User user)` - 사용자의 특정 사업장 조회
- `@Query Page<Site> searchByUserAndKeyword(...)` - 사업장 검색 (이름/주소 LIKE 검색)
- `Page<Site> findByUserAndType(User user, String type, Pageable pageable)` - 업종별 사업장 조회
- `long countByUser(User user)` - 사용자의 전체 사업장 수

**저장 메서드**:
- `save(Site)` - 사업장 저장/수정 (JPA 기본 메서드)

**삭제 메서드**:
- `delete(Site)` - 사업장 삭제 (JPA 기본 메서드)

**사용 위치**:
- SiteService: 사업장 CRUD
- DashboardService: 대시보드 요약
- AnalysisService: 사업장 권한 확인

---

### 5. AnalysisJobRepository

**위치**: [domain/analysis/repository/AnalysisJobRepository.java](src/main/java/com/skax/physicalrisk/domain/analysis/repository/AnalysisJobRepository.java)

**엔티티**: AnalysisJob

**조회 메서드**:
- `Optional<AnalysisJob> findByJobId(String jobId)` - FastAPI 작업 ID로 조회
- `Optional<AnalysisJob> findFirstBySite(Site site)` - 사업장의 작업 조회
- `List<AnalysisJob> findBySiteAndStatus(Site site, JobStatus status)` - 사업장의 특정 상태 작업 조회
- `boolean existsBySiteAndStatus(Site site, JobStatus status)` - 실행 중인 작업 존재 여부

**저장 메서드**:
- `save(AnalysisJob)` - 작업 저장 (JPA 기본 메서드)

**사용 위치**:
- AnalysisJobPollingService: 비동기 분석 작업 폴링

---

### 6. AnalysisResultRepository

**위치**: [domain/analysis/repository/AnalysisResultRepository.java](src/main/java/com/skax/physicalrisk/domain/analysis/repository/AnalysisResultRepository.java)

**엔티티**: AnalysisResult

**조회 메서드**:
- `List<AnalysisResult> findBySite(Site site)` - 사업장의 모든 분석 결과
- `Optional<AnalysisResult> findBySiteAndHazardType(Site site, String hazardType)` - 특정 위험 요인 분석 결과
- `Optional<AnalysisResult> findFirstBySiteOrderByAnalyzedAtDesc(Site site)` - 사업장의 최근 분석 결과

**저장 메서드**:
- `save(AnalysisResult)` - 분석 결과 저장 (JPA 기본 메서드)

**사용 위치**:
- (현재 Service에서 직접 사용되지 않음 - FastAPI에서 분석 결과 관리)

---

### 7. ReportRepository

**위치**: [domain/report/repository/ReportRepository.java](src/main/java/com/skax/physicalrisk/domain/report/repository/ReportRepository.java)

**엔티티**: Report

**조회 메서드**:
- `Page<Report> findBySite(Site site, Pageable pageable)` - 사업장의 리포트 목록 (페이징)
- `List<Report> findByExpiresAtBefore(LocalDateTime now)` - 만료된 리포트 조회

**저장 메서드**:
- `save(Report)` - 리포트 저장 (JPA 기본 메서드)

**사용 위치**:
- (현재 Service에서 직접 사용되지 않음 - FastAPI에서 리포트 관리)

---

### 8. IndustryRepository

**위치**: [domain/meta/repository/IndustryRepository.java](src/main/java/com/skax/physicalrisk/domain/meta/repository/IndustryRepository.java)

**엔티티**: Industry

**조회 메서드**:
- `findAll()` - 모든 산업 분류 조회 (JPA 기본 메서드)
- `Optional<Industry> findByCode(String code)` - 코드로 산업 분류 조회

**사용 위치**:
- MetaService: 메타데이터 조회
- DataInitializer: 초기 데이터 설정

---

### 9. HazardTypeRepository

**위치**: [domain/meta/repository/HazardTypeRepository.java](src/main/java/com/skax/physicalrisk/domain/meta/repository/HazardTypeRepository.java)

**엔티티**: HazardType

**조회 메서드**:
- `findAll()` - 모든 위험 요인 조회 (JPA 기본 메서드)
- `Optional<HazardType> findByCode(String code)` - 코드로 위험 요인 조회

**사용 위치**:
- MetaService: 메타데이터 조회
- DataInitializer: 초기 데이터 설정

---

## Service 계층

비즈니스 로직을 처리하며 Repository를 통해 데이터베이스에 접근합니다.

### 1. AuthService

**위치**: [service/user/AuthService.java](src/main/java/com/skax/physicalrisk/service/user/AuthService.java)

**사용 Repository**:
- UserRepository
- RefreshTokenRepository
- PasswordResetTokenRepository

**주요 DB 작업**:

#### 회원가입 (`register`)
```java
@Transactional
public String register(RegisterRequest request)
```
- **조회**: `userRepository.existsByEmail()` - 이메일 중복 확인
- **저장**: `userRepository.save(User)` - 새 사용자 저장

#### 로그인 (`login`)
```java
@Transactional
public LoginResponse login(LoginRequest request)
```
- **조회**: `userRepository.findByEmail()` - 이메일로 사용자 조회
- **저장**: `refreshTokenRepository.save(RefreshToken)` - Refresh Token 저장

#### 로그아웃 (`logout`)
```java
@Transactional
public void logout(String userId)
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **수정**: `refreshTokenRepository.revokeAllByUser()` - 모든 토큰 폐기

#### 토큰 갱신 (`refresh`)
```java
@Transactional
public LoginResponse refresh(String refreshToken)
```
- **조회**: `refreshTokenRepository.findByToken()` - 토큰 조회
- **수정**: `tokenEntity.revoke()` - 기존 토큰 폐기
- **저장**: `refreshTokenRepository.save(RefreshToken)` - 새 토큰 저장

#### 비밀번호 재설정 요청 (`requestPasswordReset`)
```java
@Transactional
public void requestPasswordReset(PasswordResetRequest request)
```
- **조회**: `userRepository.findByEmail()` - 사용자 조회
- **저장**: `passwordResetTokenRepository.save(PasswordResetToken)` - 재설정 토큰 저장

#### 비밀번호 재설정 확인 (`confirmPasswordReset`)
```java
@Transactional
public void confirmPasswordReset(PasswordResetConfirmRequest request)
```
- **조회**: `passwordResetTokenRepository.findByToken()` - 토큰 조회
- **수정**: `user.updatePassword()` - 비밀번호 변경
- **저장**: `userRepository.save(User)` - 사용자 저장
- **수정**: `resetToken.markAsUsed()` - 토큰 사용 처리

---

### 2. UserService

**위치**: [service/user/UserService.java](src/main/java/com/skax/physicalrisk/service/user/UserService.java)

**사용 Repository**:
- UserRepository

**주요 DB 작업**:

#### 현재 사용자 조회 (`getCurrentUser`)
```java
@Transactional(readOnly = true)
public UserResponse getCurrentUser()
```
- **조회**: `userRepository.findById()` - 현재 사용자 조회

#### 사용자 정보 수정 (`updateUser`)
```java
@Transactional
public UserResponse updateUser(UpdateUserRequest request)
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **수정**: `user.setLanguage()` - 언어 설정 변경
- **저장**: `userRepository.save(User)` - 변경사항 저장

#### 사용자 삭제 (`deleteUser`)
```java
@Transactional
public void deleteUser()
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **삭제**: `userRepository.delete(User)` - 사용자 삭제

---

### 3. SiteService

**위치**: [service/site/SiteService.java](src/main/java/com/skax/physicalrisk/service/site/SiteService.java)

**사용 Repository**:
- SiteRepository
- UserRepository

**주요 DB 작업**:

#### 사업장 목록 조회 (`getSites`)
```java
@Transactional(readOnly = true)
public SiteResponse getSites()
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **조회**: `siteRepository.findByUser()` - 사용자의 모든 사업장 조회

#### 사업장 생성 (`createSite`)
```java
@Transactional
public SiteResponse.SiteInfo createSite(CreateSiteRequest request)
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **저장**: `siteRepository.save(Site)` - 새 사업장 저장

#### 사업장 수정 (`updateSite`)
```java
@Transactional
public SiteResponse.SiteInfo updateSite(UUID siteId, UpdateSiteRequest request)
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **조회**: `siteRepository.findByIdAndUser()` - 사업장 조회 및 권한 확인
- **수정**: `site.setXxx()` - 필드 수정
- **저장**: `siteRepository.save(Site)` - 변경사항 저장

#### 사업장 삭제 (`deleteSite`)
```java
@Transactional
public void deleteSite(UUID siteId)
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **조회**: `siteRepository.findByIdAndUser()` - 사업장 조회 및 권한 확인
- **삭제**: `siteRepository.delete(Site)` - 사업장 삭제

---

### 4. AnalysisService

**위치**: [service/analysis/AnalysisService.java](src/main/java/com/skax/physicalrisk/service/analysis/AnalysisService.java)

**사용 Repository**:
- UserRepository
- SiteRepository
- (FastAPI 클라이언트를 통한 외부 API 호출 - DB 직접 저장 없음)

**주요 DB 작업**:

#### 분석 시작 (`startAnalysis`)
```java
@Transactional(readOnly = true)
public AnalysisJobStatusResponse startAnalysis(...)
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **조회**: `siteRepository.findByIdAndUser()` - 사업장 조회 및 권한 확인
- **외부 API**: FastAPI로 분석 요청 전송 (DB 저장 없음)

#### 분석 상태 조회 (`getAnalysisStatus`)
```java
@Transactional(readOnly = true)
public AnalysisJobStatusResponse getAnalysisStatus(UUID siteId, UUID jobId)
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **조회**: `siteRepository.findByIdAndUser()` - 권한 확인
- **외부 API**: FastAPI에서 분석 상태 조회

#### 기타 분석 결과 조회 메서드
- `getDashboardSummary()` - 대시보드 요약
- `getPhysicalRiskScores()` - 물리적 리스크 점수
- `getPastEvents()` - 과거 재난 이력
- `getFinancialImpact()` - 재무 영향
- `getVulnerability()` - 취약성 분석
- `getTotalAnalysis()` - 통합 분석 결과

**모두 DB 조회는 권한 확인용이며, 실제 분석 데이터는 FastAPI에서 관리**

---

### 5. ReportService

**위치**: [service/report/ReportService.java](src/main/java/com/skax/physicalrisk/service/report/ReportService.java)

**사용 Repository**:
- UserRepository
- (FastAPI 클라이언트를 통한 외부 API 호출 - DB 직접 저장 없음)

**주요 DB 작업**:

#### 리포트 생성 (`createReport`)
```java
@Transactional
public Map<String, Object> createReport(CreateReportRequest request)
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **외부 API**: FastAPI로 리포트 생성 요청 (DB 저장 없음)

#### 리포트 조회 (`getReportWebView`, `getReportPdf`)
```java
@Transactional(readOnly = true)
public ReportWebViewResponse getReportWebView()
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **외부 API**: FastAPI에서 리포트 조회

#### 리포트 삭제 (`deleteReport`)
```java
@Transactional
public void deleteReport()
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **외부 API**: FastAPI로 리포트 삭제 요청

---

### 6. DashboardService

**위치**: [service/site/DashboardService.java](src/main/java/com/skax/physicalrisk/service/site/DashboardService.java)

**사용 Repository**:
- UserRepository
- SiteRepository

**주요 DB 작업**:

#### 대시보드 요약 조회 (`getDashboardSummary`)
```java
@Transactional(readOnly = true)
public Map<String, Object> getDashboardSummary()
```
- **조회**: `userRepository.findById()` - 사용자 조회
- **조회**: `siteRepository.countByUser()` - 사용자의 전체 사업장 수 조회

---

### 7. MetaService

**위치**: [service/meta/MetaService.java](src/main/java/com/skax/physicalrisk/service/meta/MetaService.java)

**사용 Repository**:
- HazardTypeRepository
- IndustryRepository

**주요 DB 작업**:

#### 위험 유형 조회 (`getAllHazardTypes`)
```java
@Transactional(readOnly = true)
public List<HazardType> getAllHazardTypes()
```
- **조회**: `hazardTypeRepository.findAll()` - 모든 위험 유형 조회

#### 산업 분류 조회 (`getAllIndustries`)
```java
@Transactional(readOnly = true)
public List<Industry> getAllIndustries()
```
- **조회**: `industryRepository.findAll()` - 모든 산업 분류 조회

---

## 트랜잭션 관리

### @Transactional 사용 패턴

#### 1. readOnly = true (조회 전용)
```java
@Transactional(readOnly = true)
public class SomeService {
    // 조회만 수행하는 메서드
}
```
- 조회 전용 트랜잭션
- 성능 최적화 (플러시 모드 비활성화)
- 더티 체킹 비활성화

**사용 위치**:
- UserService: `getCurrentUser()`
- SiteService: `getSites()`
- AnalysisService: 대부분의 조회 메서드
- ReportService: `getReportWebView()`, `getReportPdf()`
- DashboardService: `getDashboardSummary()`
- MetaService: 모든 메서드

#### 2. @Transactional (쓰기 가능)
```java
@Transactional
public void updateUser(UpdateUserRequest request)
```
- 데이터 수정/삭제/생성이 필요한 메서드
- 자동 커밋/롤백
- 더티 체킹 활성화

**사용 위치**:
- AuthService: `register()`, `login()`, `logout()`, `refresh()`, 비밀번호 재설정
- UserService: `updateUser()`, `deleteUser()`
- SiteService: `createSite()`, `updateSite()`, `deleteSite()`
- ReportService: `createReport()`, `deleteReport()`

### JPA 영속성 컨텍스트

1. **1차 캐시**: 트랜잭션 내에서 같은 엔티티 재조회 시 캐시 사용
2. **더티 체킹**: `@Transactional` 내에서 엔티티 변경 시 자동 UPDATE
3. **쓰기 지연**: 트랜잭션 커밋 시점에 SQL 실행

### 주요 쿼리 최적화 기법

1. **@Query 사용**: 커스텀 JPQL 쿼리 (SiteRepository)
2. **@Modifying**: UPDATE/DELETE 쿼리 (RefreshTokenRepository)
3. **findFirst/findTop**: 단일 결과 조회 (AnalysisJobRepository, AnalysisResultRepository)
4. **existsBy**: 존재 여부만 확인 (UserRepository, AnalysisJobRepository)
5. **countBy**: 개수만 조회 (SiteRepository)

---

## 데이터 흐름 요약

### 1. 인증 흐름
```
회원가입: UserRepository.save() → User 생성
로그인: UserRepository.findByEmail() → RefreshTokenRepository.save() → Token 저장
토큰 갱신: RefreshTokenRepository.findByToken() → revoke() → save() → 새 Token
```

### 2. 사업장 관리 흐름
```
생성: UserRepository.findById() → SiteRepository.save() → Site 생성
조회: UserRepository.findById() → SiteRepository.findByUser() → Site 목록
수정: SiteRepository.findByIdAndUser() → setXxx() → save() → 변경 반영
삭제: SiteRepository.findByIdAndUser() → delete() → 삭제
```

### 3. 분석 흐름
```
분석 시작: SiteRepository.findByIdAndUser() → FastAPI 호출 (DB 저장 없음)
상태 조회: SiteRepository 권한 확인 → FastAPI 조회
결과 조회: SiteRepository 권한 확인 → FastAPI 조회
```

### 4. 리포트 흐름
```
생성: UserRepository.findById() → FastAPI 호출 (DB 저장 없음)
조회: UserRepository.findById() → FastAPI 조회
삭제: UserRepository.findById() → FastAPI 삭제
```

---

## 주요 특징

### 1. 권한 확인 패턴
모든 Service 메서드에서 사용자 인증 및 권한 확인:
```java
UUID userId = SecurityUtil.getCurrentUserId();
User user = userRepository.findById(userId)
    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
```

### 2. 외부 API 연동
- AnalysisService, ReportService는 DB 직접 저장이 아닌 FastAPI를 통한 외부 시스템 연동
- DB는 권한 확인 및 사용자/사업장 정보 조회에만 사용

### 3. 메타데이터 관리
- Industry, HazardType은 읽기 전용 메타데이터
- 애플리케이션 시작 시 DataInitializer로 초기화

### 4. 토큰 관리
- RefreshToken은 DB에 저장하여 로그아웃/갱신 시 관리
- PasswordResetToken은 임시 토큰으로 30분 유효
- 주기적으로 만료된 토큰 정리 (TokenCleanupScheduler)

---

## 개선 제안

### 1. N+1 문제 방지
- Site 조회 시 User 연관관계 fetch join 고려
- 페이징 조회 시 카운트 쿼리 최적화

### 2. 캐시 활용
- MetaService (Industry, HazardType)는 변경 빈도가 낮으므로 캐시 적용 가능
- `@Cacheable` 어노테이션 활용

### 3. Soft Delete 고려
- User, Site 삭제 시 물리 삭제 대신 논리 삭제(deleted flag) 고려
- 데이터 복구 및 감사(audit) 요구사항 대응

### 4. Batch 작업 최적화
- 대량 토큰 삭제 시 배치 크기 조정
- `spring.jpa.properties.hibernate.jdbc.batch_size` 설정 활용

### 5. 쿼리 성능 모니터링
- 느린 쿼리 로깅 설정
- JPA 통계 활성화하여 쿼리 개수 모니터링

---

**문서 작성일**: 2025-12-08
**작성자**: Claude Code
**버전**: 1.0
