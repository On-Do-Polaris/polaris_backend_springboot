# Swagger v0.2 불일치 문제점 정리

**작성일**: 2025-12-11
**참조 파일**: `docs/oas_v0.2.yaml`

---

## 🔴 문제점 요약 (5개)

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

- [ ] GET /api/past - id 타입 변경 (String → Integer)
- [ ] GET /api/analysis/status - siteId 파라미터 제거
- [ ] GET /api/simulation/location/recommendation - Response DTO 재작성
- [ ] GET /api/meta/industries - Industry 엔티티 확인
- [ ] GET /api/meta/hazards - HazardType 엔티티 확인
- [ ] 빌드 및 검증
- [ ] Swagger UI에서 실제 응답 형식 확인

---

**다음 단계**: Priority 1 작업부터 시작
