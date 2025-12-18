
# FastAPI CI/CD 파이프라인 시뮬레이션 결과

이 문서는 `TC/test_fastapi_cicd.yaml` 워크플로우가 실행될 때, 명세서의 각 테스트 케이스(TC) 시나리오별로 예상되는 동작과 로그 결과를 설명합니다.

---

### **TC_001: 파이프라인 통합 성공**

- **시나리오**: 모든 코드가 정상이고, 보안 취약점이 없는 `main` 브랜치 병합 커밋
- **예상 결과**: `test-and-lint`, `build-and-scan`, `deploy` 모든 잡(Job)이 순차적으로 성공합니다.

- **시뮬레이션 로그 (마지막 `deploy` 잡의 Health Check 단계)**:
  ```log
  Run echo "Starting health check..."
  Starting health check...
  Health check failed with status 000. Retrying in 5 seconds...
  Health check failed with status 000. Retrying in 5 seconds...
  Health check PASSED with status 200.
  ```

---

### **TC_002: 의존성 누락 차단**

- **시나리오**: `requirements.txt`에 없는 패키지를 `import`한 코드를 커밋합니다.
- **예상 결과**: `test-and-lint` 잡의 `uv sync` 단계에서 실패하며, 파이프라인이 즉시 중단됩니다.

- **시뮬레이션 로그 (`uv sync` 단계)**:
  ```log
  Run uv sync --strict
  error: Failed to find `numpy` in the requirements file.
  --> `main.py:2:1`
    |
  2 | import numpy as np
    | ^^^^^^^^^^^^^^^^^^ `numpy` is not a direct dependency.
  ```

---

### **TC_007: Lint 규칙 위반 차단**

- **시나리오**: PEP8 스타일 가이드(예: 과도한 공백)를 위반한 코드를 커밋합니다.
- **예상 결과**: `test-and-lint` 잡의 `Lint with Ruff` 단계에서 실패하며, 파이프라인이 중단됩니다.

- **시뮬레이션 로그 (`Ruff` 실행 단계)**:
  ```log
  Run uv run ruff check . --exit-non-zero-on-fix
  Found 1 error (1 fixable).
  main.py:10:5: E302 expected 2 blank lines, found 1
  Error: Process completed with exit code 1.
  ```

---

### **TC_008: 시크릿 마스킹 확인**

- **시나리오**: 워크플로우에서 GitHub Actions 시크릿을 `echo` 명령어로 출력합니다.
- **예상 결과**: 실제 시크릿 값이 노출되지 않고, 로그에 `***`로 마스킹되어 출력됩니다.

- **시뮬레이션 로그 (`Verify Secret Masking` 단계)**:
  ```log
  Run echo "Secret token starts with: ***"
  Secret token starts with: ***
  ```

---

### **TC_009: 이미지 보안 스캔**

- **시나리오**: 빌드된 Docker 이미지의 기반 OS 또는 라이브러리에서 `High` 또는 `Critical` 등급의 보안 취약점이 발견됩니다.
- **예상 결과**: `build-and-scan` 잡의 `Scan image with Trivy` 단계에서 실패하며, 배포가 중단됩니다.

- **시뮬레이션 로그 (`Trivy` 스캔 단계)**:
  ```log
  Run aquasecurity/trivy-action@master
  ...
  your-fastapi-app:abcdef123 (debian 11.5)
  =======================================
  Total: 1 (HIGH: 1, CRITICAL: 0)

  +----------------+------------------+----------+-------------------+---------------+---------------------------------------+
  |    LIBRARY     | VULNERABILITY ID | SEVERITY | INSTALLED VERSION | FIXED VERSION |                 TITLE                 |
  +----------------+------------------+----------+-------------------+---------------+---------------------------------------+
  | openssl        | CVE-2022-3602    | HIGH     | 3.0.5-2           | 3.0.7-1       | openssl: X.509 Email Address...       |
  +----------------+------------------+----------+-------------------+---------------+---------------------------------------+

  Error: Trivy found vulnerabilities with severity 'CRITICAL,HIGH'.
  Error: Process completed with exit code 1.
  ```

---

### **TC_010: 배포 후 가용성 검증**

- **시나리오**: 배포 스크립트 실행 후, 웹 서비스가 완전히 뜨기까지 약간의 시간이 걸립니다.
- **예상 결과**: `deploy` 잡의 `Health Check` 단계에서 `curl` 명령어가 몇 차례 재시도 후, 최종적으로 `200 OK`를 받아 성공 처리됩니다. 만약 제한 시간 내에 성공하지 못하면 파이프라인은 실패합니다.

- **시뮬레이션 로그 (`Health Check` 단계)**:
  ```log
  Run echo "Starting health check..."
  Starting health check...
  Health check failed with status 502. Retrying in 5 seconds...
  Health check failed with status 502. Retrying in 5 seconds...
  Health check PASSED with status 200.
  ```
