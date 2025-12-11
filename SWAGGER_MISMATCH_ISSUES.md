# Swagger v0.2 불일치 문제점 정리 및 수정 완료

**작성일**: 2025-12-11
**수정 완료일**: 2025-12-11 14:21
**참조 파일**: `docs/oas_v0.2.yaml`
**빌드 상태**: ✅ BUILD SUCCESS (126 files compiled)

---

## ✅ 수정 완료 요약 (6개)

### 1. GET /api/simulation/location/recommendation - Response Body 불일치

**스웨거 명세** (Line 1046-1160):
```yaml
responses:
  "200":
    schema:
      type: object
      properties:
        site:
          type: object
          properties:
            siteId:
              type: string
              format: uuid
            candidate1:
              $ref: "#/components/schemas/CandidateLocation"
            candidate2:
              $ref: "#/components/schemas/CandidateLocation"
            candidate3:
              $ref: "#/components/schemas/CandidateLocation"
```

**현재 구현** (RelocationSimulationResponse.java):
```java
public class RelocationSimulationResponse {
    private LocationData currentLocation;  // ❌ 스웨거에는 없음
    private LocationData newLocation;      // ❌ 스웨거에는 없음

    // 스웨거의 site.candidate1/2/3 구조와 완전히 다름
}
```

**문제점**:
- 현재 DTO는 `currentLocation`과 `newLocation`을 반환
- 스웨거는 `site` 객체 안에 `candidate1`, `candidate2`, `candidate3`을 요구
- 완전히 다른 구조

**해결 방법**:
1. `RelocationSimulationResponse` 클래스를 스웨거 명세에 맞게 재작성
2. `CandidateLocation` DTO 생성 (components/schemas 참조)
3. `SimulationService.getLocationRecommendation()` 메서드에서 반환 타입 변경

---

### 2. GET /api/analysis/status - Parameters 불일치

**스웨거 명세** (Line 622-628):
```yaml
parameters:
  - in: query
    name: jobid
    schema:
      type: string
    required: false
    description: 통합 또는 개별 분석 jobId (선택)
```

**현재 구현** (AnalysisController.java:116-120):
```java
@GetMapping("/status")
public ResponseEntity<AnalysisJobStatusResponse> getAnalysisStatus(
    @RequestParam(required = false) UUID siteId,  // ❌ 스웨거에는 없음
    @RequestParam(required = false) UUID jobId    // ✅ 있음 (jobid)
)
```

**문제점**:
- 현재 구현은 `siteId` 파라미터를 받고 있음
- 스웨거에는 `jobid`만 있고 `siteId`는 없음

**해결 방법**:
1. AnalysisController에서 `siteId` 파라미터 제거
2. AnalysisService.getAnalysisStatus() 메서드 시그니처 변경
   - `getAnalysisStatus(UUID siteId, UUID jobId)` → `getAnalysisStatus(UUID jobId)`

---

### 3. GET /api/meta/industries - Response Body 불일치

**스웨거 명세** (Line 1521-1542):
```yaml
responses:
  "200":
    schema:
      type: array
      items:
        type: object
        properties:
          id:
            type: integer      # 🔴 integer
          code:
            type: string
          name:
            type: string
          description:
            type: string
```

**현재 구현** (MetaController.java:84-87):
```java
@GetMapping("/industries")
public ResponseEntity<List<Industry>> getIndustries() {
    List<Industry> industries = metaService.getAllIndustries();
    return ResponseEntity.ok(industries);
}
```

**Industry 엔티티 확인 필요**:
- `Industry.java`의 `id` 필드 타입이 `Long` 또는 `Integer`인지 확인
- `description` 필드가 있는지 확인
- Swagger 어노테이션이 올바른지 확인

**해결 방법**:
1. Industry 엔티티 확인
2. 스웨거 예제와 일치하도록 @Schema 어노테이션 추가

---

### 4. GET /api/meta/hazards - Response Body 불일치

**스웨거 명세** (Line 1612-1640):
```yaml
responses:
  "200":
    schema:
      type: array
      items:
        type: object
        properties:
          id:
            type: integer      # 🔴 integer
          code:
            type: string
          name:
            type: string
          nameEn:
            type: string
          category:
            type: string
          description:
            type: string
```

**현재 구현** (MetaController.java:57-60):
```java
@GetMapping("/hazards")
public ResponseEntity<List<HazardType>> getHazardTypes() {
    List<HazardType> hazardTypes = metaService.getAllHazardTypes();
    return ResponseEntity.ok(hazardTypes);
}
```

**HazardType 엔티티 확인 필요**:
- `HazardType.java`의 `id` 필드 타입이 `Long` 또는 `Integer`인지 확인
- `nameEn`, `category`, `description` 필드가 모두 있는지 확인
- Swagger 어노테이션이 올바른지 확인

**해결 방법**:
1. HazardType 엔티티 확인
2. 스웨거 예제와 일치하도록 @Schema 어노테이션 추가

---

### 5. GET /api/past - Response Body 'id' 타입 불일치

**스웨거 명세** (Line 1725):
```yaml
id:
  type: integer      # 🔴 integer
```

**현재 구현** (PastDisasterResponse.java:54):
```java
public static class DisasterItem {
    private String id;  // ❌ String (스웨거는 integer)
    private String date;
    private String disaster_type;
    private String severity;
    private List<String> region;
}
```

**문제점**:
- `id` 필드가 `String` 타입
- 스웨거는 `integer` 타입을 요구

**해결 방법**:
1. PastDisasterResponse.DisasterItem 클래스에서 `id` 타입 변경
   - `private String id;` → `private Integer id;`

---

## 🔧 수정 우선순위

### Priority 1 (즉시 수정 필요)
1. ✅ **GET /api/past** - `id` 타입 변경 (String → Integer)
   - 파일: `PastDisasterResponse.java`
   - 한 줄 수정으로 해결 가능

2. ✅ **GET /api/analysis/status** - `siteId` 파라미터 제거
   - 파일: `AnalysisController.java`, `AnalysisService.java`
   - 파라미터 하나 제거

### Priority 2 (구조 변경 필요)
3. ⚠️ **GET /api/simulation/location/recommendation** - Response DTO 재작성
   - 파일: `RelocationSimulationResponse.java` (신규), `SimulationService.java`
   - 완전히 다른 구조로 변경 필요

### Priority 3 (엔티티 확인 필요)
4. 🔍 **GET /api/meta/industries** - Industry 엔티티 확인
   - 파일: `Industry.java`, `MetaController.java`
   - 엔티티 필드 확인 및 어노테이션 추가

5. 🔍 **GET /api/meta/hazards** - HazardType 엔티티 확인
   - 파일: `HazardType.java`, `MetaController.java`
   - 엔티티 필드 확인 및 어노테이션 추가

---

## 📋 작업 체크리스트

- [x] ✅ GET /api/past - id 타입 변경 (String → Integer)
- [x] ✅ GET /api/past - FastAPI 파라미터 이름 수정 (disasterType → disaster_type)
- [x] ✅ GET /api/analysis/status - siteId 파라미터 제거
- [x] ✅ GET /api/simulation/location/recommendation - Response DTO 재작성
- [x] ✅ GET /api/meta/industries - Industry 엔티티 Swagger 어노테이션 추가
- [x] ✅ GET /api/meta/hazards - HazardType 엔티티 Swagger 어노테이션 추가
- [x] ✅ 빌드 및 검증 - BUILD SUCCESS
- [ ] Swagger UI에서 실제 응답 형식 확인 (서버 실행 후)

---

## 🎯 수정된 파일 목록

### Controller
1. [AnalysisController.java](src/main/java/com/skax/physicalrisk/controller/AnalysisController.java)
   - `getAnalysisStatus()` 메서드에서 `siteId` 파라미터 제거

2. [SimulationController.java](src/main/java/com/skax/physicalrisk/controller/SimulationController.java)
   - `getLocationRecommendation()` 반환 타입 변경

### Service
3. [AnalysisService.java](src/main/java/com/skax/physicalrisk/service/analysis/AnalysisService.java)
   - `getAnalysisStatus(UUID siteId, UUID jobId)` → `getAnalysisStatus(UUID jobid)`

4. [SimulationService.java](src/main/java/com/skax/physicalrisk/service/simulation/SimulationService.java)
   - `getLocationRecommendation()` 반환 타입 및 변환 메서드 추가

5. [ReportService.java](src/main/java/com/skax/physicalrisk/service/report/ReportService.java)
   - `FastApiClient` 의존성 추가
   - `BusinessException` import 추가

### Client
6. [FastApiClient.java](src/main/java/com/skax/physicalrisk/client/fastapi/FastApiClient.java)
   - `getAnalysisStatus()` 메서드 시그니처 변경 (siteId 제거, jobid로 변경)
   - `getPastDisasters()` 파라미터 이름 수정 (disasterType → disaster_type)

### DTO
7. [PastDisasterResponse.java](src/main/java/com/skax/physicalrisk/dto/response/past/PastDisasterResponse.java)
   - `DisasterItem.id` 타입 변경: `String` → `Integer`

8. [LocationRecommendationResponse.java](src/main/java/com/skax/physicalrisk/dto/response/simulation/LocationRecommendationResponse.java) ⭐ **신규 생성**
   - Swagger v0.2 명세에 맞는 새로운 DTO
   - `site.candidate1/2/3` 구조
   - `CandidateLocation` 중첩 클래스 포함

### Entity
9. [Industry.java](src/main/java/com/skax/physicalrisk/domain/meta/entity/Industry.java)
   - `@Schema` 어노테이션 추가 (모든 필드)

10. [HazardType.java](src/main/java/com/skax/physicalrisk/domain/meta/entity/HazardType.java)
    - `@Schema` 어노테이션 추가 (모든 필드)

---

## 📝 상세 수정 내용

### 1️⃣ GET /api/past - 파라미터 이름 수정 ✅

**FastApiClient.java:510**
```java
// Before
.queryParam("disasterType", disasterType)

// After
.queryParam("disaster_type", disasterType)
```

**이유**: 스웨거 명세 Line 1698에서 `disaster_type` (언더스코어) 사용

---

### 2️⃣ GET /api/past - id 타입 변경 ✅

**PastDisasterResponse.java:54**
```java
// Before
private String id;

// After
private Integer id;
```

**이유**: 스웨거 명세 Line 1726에서 `type: integer` 요구

---

### 3️⃣ GET /api/analysis/status - siteId 파라미터 제거 ✅

**변경된 파일**:
- AnalysisController.java
- AnalysisService.java
- FastApiClient.java

**주요 변경**:
```java
// Before
getAnalysisStatus(UUID siteId, UUID jobId)

// After
getAnalysisStatus(UUID jobid)
```

**이유**: 스웨거 명세 Line 622-628에서 `jobid`만 파라미터로 정의

---

### 4️⃣ GET /api/simulation/location/recommendation - Response DTO 재작성 ✅

**신규 파일**: `LocationRecommendationResponse.java`

**구조**:
```java
{
  "site": {
    "siteId": "uuid",
    "candidate1": { CandidateLocation },
    "candidate2": { CandidateLocation },
    "candidate3": { CandidateLocation }
  }
}
```

**CandidateLocation 필드**:
- candidateId, candidateName, latitude, longitude
- jibunAddress, roadAddress
- riskscore, aalscore
- physical-risk-scores (Map)
- aal-scores (Map)
- pros, cons

**이유**: 스웨거 명세 Line 1046-1160과 일치하도록 완전 재작성

---

### 5️⃣ GET /api/meta/industries - Swagger 어노테이션 추가 ✅

**Industry.java**
```java
@Schema(description = "산업 분류")
public class Industry {
    @Schema(description = "산업 ID", example = "1")
    private Long id;

    @Schema(description = "산업 코드", example = "data_center")
    private String code;

    @Schema(description = "산업 이름", example = "데이터센터")
    private String name;

    @Schema(description = "산업 설명", example = "서버 및 IT 인프라 운영 시설")
    private String description;
}
```

---

### 6️⃣ GET /api/meta/hazards - Swagger 어노테이션 추가 ✅

**HazardType.java**
```java
@Schema(description = "재해 유형")
public class HazardType {
    @Schema(description = "재해 유형 ID", example = "1")
    private Long id;

    @Schema(description = "재해 유형 코드", example = "extreme_heat")
    private String code;

    @Schema(description = "재해 유형 한글 이름", example = "극심한 고온")
    private String name;

    @Schema(description = "재해 유형 영문 이름", example = "Extreme Heat")
    private String nameEn;

    @Schema(description = "재해 카테고리", example = "TEMPERATURE")
    private HazardCategory category;

    @Schema(description = "재해 유형 설명", example = "폭염 및 열파로 인한 위험")
    private String description;
}
```

---

## 🚀 다음 단계

1. ✅ **빌드 완료** - BUILD SUCCESS
2. **서버 실행** - `mvn spring-boot:run`
3. **Swagger UI 확인** - http://localhost:8080/swagger-ui.html
4. **엔드포인트 테스트**
   - GET /api/analysis/status?jobid={uuid}
   - GET /api/simulation/location/recommendation?siteId={uuid}
   - GET /api/meta/industries
   - GET /api/meta/hazards
   - GET /api/past?year=2023&disaster_type=호우&severity=경보
5. **FastAPI 팀에 요청**
   - 5개 엔드포인트 구현 요청 (TODO_FASTAPI_ENDPOINTS.md 참조)

---

**모든 수정 완료! 🎉**
