## Swagger 어노테이션 수정 가이드

이 문서는 실제 API 동작과 Swagger API 문서 간의 불일치를 해결하기 위한 상세 가이드입니다.
`GlobalExceptionHandler`와 각 컨트롤러의 로직을 분석한 결과를 바탕으로 작성되었습니다.

### 공통 가이드라인

1.  **400 Bad Request (잘못된 요청)**
    *   **대상**: 요청 본문(`@RequestBody`)에 `@Valid` 어노테이션이 있는 모든 API.
    *   **사유**: 유효성 검사 실패 시 `GlobalExceptionHandler`가 전역적으로 **400 에러**를 반환합니다.
    *   **조치**: 해당 API의 `@ApiResponse` 목록에 `responseCode = "400"`을 추가해야 합니다.

2.  **500 Internal Server Error (서버 내부 오류)**
    *   **대상**: 모든 API.
    *   **사유**: 예측하지 못한 서버 내부 예외가 발생할 수 있습니다.
    *   **조치**: 대부분 이미 명시되어 있지만, 누락된 경우 추가를 권장합니다.

3.  **기타 비즈니스 에러 코드**
    *   **대상**: 서비스 로직을 호출하는 모든 API.
    *   **사유**: 서비스 로직에서 발생하는 특정 예외(`BusinessException`)는 `ErrorCode`에 따라 다양한 HTTP 상태 코드(401, 404, 409, 422, 503 등)로 변환됩니다.
    *   **조치**: 각 API가 호출하는 서비스 메서드를 분석하여 발생 가능한 모든 에러를 문서화해야 합니다.

---

### 컨트롤러별 상세 수정 가이드

#### 1. `HealthController.java`

*   **메서드**: `checkCors()`
*   **문제점**: 성공 응답(200)에 대한 `@ApiResponse`가 누락되었습니다.
*   **수정 제안**:
    ```java
    @Operation(
        summary = "CORS 설정 확인",
        description = "현재 애플리케이션에 적용된 CORS 허용 도메인 목록을 확인합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "CORS 설정 정보",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = "{\"allowed-origins\": \"http://localhost:3000,http://localhost:5173,https://on-do.site\"}"
                    )
            )
    )
    @GetMapping("/cors-check")
    public ResponseEntity<Map<String, String>> checkCors() { /* ... */ }
    ```

---

#### 2. `AuthController.java`

*   **공통 문제점**: 대부분의 메서드에 `@Valid`가 사용되지만, **400 Bad Request** 응답이 문서화되지 않았습니다.
*   **수정 제안**: 아래 `@ApiResponse`를 각 대상 메서드에 추가합니다.
    ```java
    @ApiResponse(
        responseCode = "400",
        description = "입력값 유효성 검사 실패",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"result\": \"error\", \"message\": \"이메일 형식에 맞지 않습니다.\", \"errorCode\": \"INVALID_REQUEST\", \"timestamp\": \"2025-12-11T15:30:00\"}")
        )
    )
    ```
*   **적용 대상 메서드**:
    *   `registerEmail(@Valid ...)`
    *   `registerVerificationCode(@Valid ...)`
    *   `register(@Valid ...)`
    *   `login(@Valid ...)`
    *   `refresh(@Valid ...)`
    *   `resetPasswordEmail(@Valid ...)`
    *   `verifyPasswordResetCode(@Valid ...)`
    *   `completePasswordReset(@Valid ...)`

---

#### 3. `AnalysisController.java`

*   **메서드**: `startAnalysis(...)`
*   **문제점**: 요청 본문 유효성 검사(400), 분석 중복 실행(409), FastAPI 통신 실패(503) 관련 에러가 누락되었습니다.
*   **수정 제안**: 아래 `@ApiResponse`들을 추가합니다.
    ```java
    @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "이미 실행 중인 분석 작업이 있음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "분석 서버(FastAPI) 연결 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ```

*   **메서드**: `notifyAnalysisCompletion(...)`
*   **문제점**: 이메일 전송 실패(503) 및 관련 사용자 정보 없음(404) 케이스가 누락되었습니다.
*   **수정 제안**: 아래 `@ApiResponse`들을 추가합니다.
    ```java
    @ApiResponse(responseCode = "404", description = "알림을 받을 사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "이메일 전송 서비스 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ```

*   **메서드**: `getAnalysisSummary`, `getPhysicalRisk`, `getAal`, `getVulnerability`
*   **문제점**: 분석 결과가 아직 존재하지 않을 경우(404)에 대한 응답이 누락되었습니다.
*   **수정 제안**: 각 메서드에 아래 `@ApiResponse`를 추가합니다.
    ```java
    @ApiResponse(responseCode = "404", description = "사업장 또는 분석 결과를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ```

---

#### 4. `ReportController.java`

*   **메서드**: `registerReportData(...)`
*   **문제점**: 잘못된 파일 형식/요청(400) 및 파일 업로드 실패(500)에 대한 문서가 부족합니다.
*   **수정 제안**: 아래 `@ApiResponse`들을 추가합니다.
    ```java
    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 지원하지 않는 파일 형식", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    // 500 응답의 description을 좀 더 구체적으로 명시
    @ApiResponse(responseCode = "500", description = "파일 업로드 실패 또는 서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ```

*   **메서드**: `getReport()`
*   **문제점**: 보고서 데이터가 아직 생성되지 않은 경우(404)가 누락되었습니다.
*   **수정 제안**: 아래 `@ApiResponse`를 추가합니다.
    ```java
    @ApiResponse(responseCode = "404", description = "보고서 데이터를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ```

---

#### 5. `SimulationController.java`

*   **공통 문제점**: 모든 메서드가 FastAPI와 통신하므로 **503 Service Unavailable** 에러가 발생할 수 있습니다. `getLocationRecommendation`, `compareLocation`, `runClimateSimulation` 모두에 503 응답이 명시되어 있는지 확인하고 누락 시 추가해야 합니다.
*   **메서드**: `compareLocation`, `runClimateSimulation`
*   **문제점**: `@Valid`를 사용하지만 **400 Bad Request** 응답이 문서화되지 않았습니다.
*   **수정 제안**: `AuthController`에서 제안된 것과 동일한 `400` 응답 `@ApiResponse`를 추가합니다.

---

#### 6. `SiteController.java`

*   **메서드**: `createSite(@Valid ...)`
*   **문제점**: `@Valid`에 대한 **400 Bad Request**가 누락되었고, 성공 응답이 `201 Created`임에도 `200`으로 잘못 표기될 수 있습니다. (현재 코드는 `200`으로 되어있음)
*   **수정 제안**:
    1.  `@Valid`에 대한 **400 응답** `@ApiResponse`를 추가합니다.
    2.  성공 응답 코드를 `201`로 수정합니다.
        ```java
        @ApiResponse(
            responseCode = "201",
            description = "사업장 등록 성공",
            content = @Content(...)
        )
        ```

*   **메서드**: `updateSite(...)`
*   **검토 결과**: `INVALID_SITE_DATA` (422)가 잘 명시되어 있어 양호합니다.

---

#### 7. 그 외 컨트롤러 (`Dashboard`, `Past`, `Sites`, `User`, `Meta`)

*   **`DashboardController`**: `analysisService.getDashboardSummary()` 호출 시, 분석 데이터가 없는 경우 **404 Not Found**를 문서화하는 것을 권장합니다.
*   **`PastController`**: `getPastDisasters()`에서 잘못된 쿼리 파라미터(예: 숫자가 아닌 `year`)가 들어올 경우 **400 Bad Request**가 발생할 수 있습니다. 현재 `400`이 명시되어 있으나, 설명을 구체화할 수 있습니다.
*   **`SitesController`, `UserController`, `MetaController`**: 검토 결과, 현재 문서화가 비교적 양호한 상태이며 특별히 누락된 주요 에러 경로는 보이지 않습니다.

이 가이드를 바탕으로 각 컨트롤러의 어노테이션을 검토하고 수정하시면 Swagger 문서가 실제 API 동작과 거의 일치하게 될 것입니다.
