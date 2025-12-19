
# Spring Boot CI/CD 파이프라인 시뮬레이션 결과

이 문서는 `TC/test_springboot_cicd.yaml` 워크플로우가 실행될 때, 명세서의 각 테스트 케이스(TC) 시나리오별로 예상되는 동작과 로그 결과를 설명합니다.

---

### **TC_001: CI/CD 전체 통합 성공**

- **시나리오**: 모든 코드가 품질 기준을 통과하고, 테스트가 성공하는 `main` 브랜치 병합 커밋.
- **예상 결과**: `build-and-test`, `build-and-push-to-gcr`, `deploy-to-cloud-run` 모든 잡(Job)이 순차적으로 성공하며 배포가 완료됩니다.

- **시뮬레이션 로그 (마지막 `deploy-to-cloud-run` 잡의 Health Check 단계)**:
  ```log
  Run echo "Starting post-deployment health check..."
  Starting post-deployment health check...
  Service not ready yet. Retrying in 5 seconds...
  Service not ready yet. Retrying in 5 seconds...
  Health check PASSED. Service is UP.
  ```

---

### **TC_002: 코드 스타일 강제**

- **시나리오**: `pom.xml`에 `maven-checkstyle-plugin`이 설정되어 있다는 전제 하에, Checkstyle 규칙을 위반한 코드를 커밋합니다.
- **예상 결과**: `build-and-test` 잡의 `Run Maven Verify` 단계에서 Checkstyle 오류가 감지되어 빌드가 실패하고 파이프라인이 중단됩니다.
- **참고**: 현재 `pom.xml`에는 이 플러그인이 없습니다. 추가해야 이 기능이 동작합니다.

- **시뮬레이션 로그 (`Run Maven Verify` 단계)**:
  ```log
  [INFO] --- maven-checkstyle-plugin:3.2.0:check (checkstyle) @ physical-risk-management ---
  [INFO] Starting audit...
  [ERROR] /home/runner/work/backend/backend/src/main/java/com/skax/physicalrisk/controller/AuthController.java:25: 'if' is not followed by whitespace. [WhitespaceAfter]
  [INFO] Audit done.
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD FAILURE
  [INFO] ------------------------------------------------------------------------
  [ERROR] Failed to execute goal org.apache.maven.plugins:maven-checkstyle-plugin:3.2.0:check (checkstyle) on project physical-risk-management: You have 1 Checkstyle violation. -> [Help 1]
  ```

---

### **TC_003: 단위 테스트 실패 감지**

- **시나리오**: 의도적으로 실패하는 JUnit 테스트 케이스(`@Test` 어노테이션)를 포함하여 커밋합니다.
- **예상 결과**: `build-and-test` 잡의 `Run Maven Verify` 단계 중 `test` 생명주기에서 테스트 실패가 감지되어 빌드가 실패하고 파이프라인이 중단됩니다.

- **시뮬레이션 로그 (`Run Maven Verify` 단계)**:
  ```log
  [INFO] --- maven-surefire-plugin:3.1.2:test (default-test) @ physical-risk-management ---
  [INFO] Running com.skax.physicalrisk.service.AuthServiceTest
  [ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.05 s <<< FAILURE! - in com.skax.physicalrisk.service.AuthServiceTest
  [ERROR] com.skax.physicalrisk.service.AuthServiceTest.testLoginFailure  Time elapsed: 0.01 s  <<< FAILURE!
  java.lang.AssertionError: expected:<401> but was:<200>
  ...
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD FAILURE
  [INFO] ------------------------------------------------------------------------
  [ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.1.2:test (default-test): There are test failures.
  ```

---

### **TC_004: 커버리지 기준 미달 차단**

- **시나리오**: 테스트 코드를 충분히 작성하지 않아 JaCoCo의 코드 커버리지 기준(예: 80%)을 충족하지 못하는 코드를 커밋합니다.
- **예상 결과**: `build-and-test` 잡의 `Run Maven Verify` 단계 중 `jacoco:check` 규칙에 따라 빌드가 실패합니다.
- **참고**: 현재 `pom.xml`의 기준은 60%(`0.60`)입니다. 이 기준을 80%로 상향해야 명세대로 동작합니다.

- **시뮬레이션 로그 (`Run Maven Verify` 단계)**:
  ```log
  [INFO] --- jacoco-maven-plugin:0.8.11:check (check) @ physical-risk-management ---
  [INFO] Analyzed bundle 'SKAX Physical Risk Management'
  [ERROR] Rule violated for bundle SKAX Physical Risk Management: instructions covered ratio is 0.78, but expected minimum is 0.80
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD FAILURE
  [INFO] ------------------------------------------------------------------------
  [ERROR] Failed to execute goal org.jacoco:jacoco-maven-plugin:0.8.11:check (check) on project physical-risk-management: Coverage check failed.
  ```

---

### **TC_005: Actuator Health Check**

- **시나리오**: 배포가 성공적으로 완료되고 서비스가 정상적으로 실행됩니다.
- **예상 결과**: `deploy-to-cloud-run` 잡의 마지막 단계인 `Post-Deployment Health Check`가 `/actuator/health` 엔드포인트를 호출하여 `{"status":"UP"}` JSON 응답을 확인하고 최종 성공 처리합니다.

- **시뮬레이션 로그 (`Post-Deployment Health Check` 단계)**:
  ```log
  Run echo "Starting post-deployment health check..."
  Starting post-deployment health check...
  Service not ready yet. Retrying in 5 seconds...
  Health check PASSED. Service is UP.
  ```

---

### **TC_006: Prod 프로필 주입 확인**

- **시나리오**: `deploy-to-cloud-run` 잡이 실행됩니다.
- **예상 결과**: `google-github-actions/deploy-cloudrun` 액션의 로그에서 `SPRING_PROFILES_ACTIVE` 환경변수가 `prod`로 설정되는 것을 확인할 수 있습니다. 컨테이너 내부의 애플리케이션은 `application-prod.yml` 설정을 우선적으로 읽게 됩니다.

- **시뮬레이션 로그 (`Deploy to Cloud Run` 단계)**:
  ```log
  Run google-github-actions/deploy-cloudrun@v2
  ...
  Updating service [physical-risk-management] (region: [asia-northeast3])...
  - Setting environment variables...
    - SPRING_PROFILES_ACTIVE=prod
  ...
  Done.
  ```

---

### **TC_007: 빌드 캐시 최적화**

- **시나리오**: 코드 변경 없이 `README.md`와 같은 문서 파일만 수정하고 커밋합니다.
- **예상 결과**: `build-and-test` 잡의 `Cache Maven packages` 단계에서 캐시를 성공적으로 복원(`Cache hit`)하여, 의존성을 다시 다운로드하지 않고 빌드 시간이 단축됩니다.

- **시뮬레이션 로그 (`Cache Maven packages` 단계)**:
  ```log
  Run actions/cache@v4
  Cache found in primary key: Linux-maven-xxxxxxxx
  Restoring Cache...
  Cache restored successfully.
  ```
