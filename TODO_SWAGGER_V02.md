# Swagger v0.2 구현 작업 현황

**마지막 업데이트**: 2025-12-11

## ✅ 완료된 작업

### 1. Swagger 어노테이션 추가 (모든 컨트롤러)
- [x] AuthController - login, refresh 엔드포인트
- [x] UserController - GET, PATCH, DELETE /api/users/me
- [x] SiteController - 모든 CRUD 엔드포인트
- [x] DashboardController - /api/dashboard/summary
- [x] AnalysisController - 모든 엔드포인트
- [x] SimulationController - 모든 엔드포인트
- [x] ReportController - GET, POST 엔드포인트
- [x] MetaController - 모든 메타데이터 엔드포인트
- [x] PastController - GET /api/past

### 2. AnalysisController 엔드포인트 재구성
- [x] **제거된 엔드포인트** (Swagger v0.2에 없음):
  - ~~GET /api/analysis/dashboard/summary~~ → DashboardController로 이동
  - ~~GET /api/analysis/physical-risk-scores~~ → `/physical-risk`로 변경
  - ~~GET /api/analysis/past-events~~ → 제거
  - ~~GET /api/analysis/financial-impacts~~ → `/aal`로 변경
  - ~~GET /api/analysis/total~~ → 제거

- [x] **추가된 엔드포인트** (Swagger v0.2 준수):
  - GET /api/analysis/summary
  - GET /api/analysis/physical-risk (기존 physical-risk-scores와 연결)
  - GET /api/analysis/aal (기존 financial-impacts와 연결)

### 3. 서비스 메서드 매핑
- [x] GET /api/analysis/physical-risk → `AnalysisService.getPhysicalRiskScores()`
- [x] GET /api/analysis/aal → `AnalysisService.getFinancialImpact()`
- [x] GET /api/analysis/summary → `AnalysisService.getAnalysisSummary()` (더미 데이터 반환)

### 4. 코드 정리
- [x] AnalysisService.getDashboardSummary() @Deprecated 처리
- [x] AnalysisService 사용되지 않는 메서드 제거 (getPastEvents, getTotalAnalysis)
- [x] 메서드 주석에 v0.2 엔드포인트 경로 명시

---

## 🔴 남은 작업 없음

모든 필수 작업이 완료되었습니다!

---

## ⚠️ 더미 데이터로 동작 중인 엔드포인트 (FastAPI 구현 필요)

### ReportController
- `GET /api/report` - ReportService.java:65 (더미 데이터 반환)
- `POST /api/report/data` - ReportService.java:102 (로컬 저장만)

### SimulationController
- `GET /api/simulation/location/recommendation` - SimulationService.java:77 (빈 객체 반환)

### PastController
- `GET /api/past` - PastDisasterService.java:70 (빈 리스트 반환)

---

## 📋 작업 우선순위

### ✅ Priority 1 (즉시 수정 필요) - 완료
1. ✅ GET /api/analysis/physical-risk 서비스 연결 (완료)
2. ✅ GET /api/analysis/aal 서비스 연결 (완료)
3. ✅ GET /api/analysis/summary 서비스 메서드 구현 (완료 - 더미 데이터)
4. ✅ MetaController 엔드포인트 확인 (이미 올바르게 구현됨)

### ✅ Priority 2 (코드 정리) - 완료
1. ✅ AnalysisService 사용되지 않는 메서드 제거/Deprecated 처리 (완료)

### ⚠️ Priority 3 (FastAPI 팀 협업 필요)
1. AnalysisService.getAnalysisSummary() 실제 FastAPI 연동
2. ReportController 실제 구현
3. SimulationController location/recommendation 실제 구현
4. PastController 실제 구현

---

## 📝 참고사항

### Swagger v0.2 문서 위치
- `docs/oas_v0.2.yaml`

### 빌드 상태
- ✅ BUILD SUCCESS (2025-12-11 09:32:38)

### 주요 변경 사항 (2025-12-11)
- AnalysisController 엔드포인트 경로 변경:
  - `/physical-risk-scores` → `/physical-risk`
  - `/financial-impacts` → `/aal`
- 모든 컨트롤러에 완전한 Swagger 어노테이션 추가 완료
- AnalysisService.getAnalysisSummary() 메서드 구현 (더미 데이터)
- AnalysisService 코드 정리 완료:
  - getPastEvents() 메서드 제거
  - getTotalAnalysis() 메서드 제거
  - getDashboardSummary() @Deprecated 처리
- MetaController 검증 완료 (이미 올바르게 구현됨)
