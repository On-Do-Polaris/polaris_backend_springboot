
# 통합 배포 및 상호 운용성 테스트 결과

이 문서는 `TC/test_integration.py` 테스트 스크립트의 실행 결과와, 자동화가 불가능한 테스트 케이스의 수동 실행 가이드를 포함합니다.

---

### **TC_001 / TC_004: 서비스 간 Health Check 및 ModelOps 연동 (E2E 스모크 테스트)**

- **시나리오**: Spring Boot의 분석 시작 API를 호출하여, 내부적으로 FastAPI 및 관련 서비스까지의 전체 호출 체인이 정상 동작하는지 확인합니다.
- **실행 결과**: 성공.
- **시뮬레이션 로그**:
  ```log
  INFO:__main__:Running TC_001/TC_004: E2E Smoke Test
  INFO:__main__:Fixture created site with ID: a1b2-c3d4-e5f6-g7h8
  INFO:__main__:TC_001/TC_004 Passed: Chained call returned a success status code (200).
  ```

---

### **TC_002: API Key 보안 강제**

- **시나리오**: FastAPI 서비스에 API Key 없이, 잘못된 키로, 그리고 정상적인 키로 각각 요청을 보내 보안 설정이 올바르게 동작하는지 검증합니다.
- **실행 결과**: 성공.
- **시뮬레이션 로그**:
  ```log
  INFO:__main__:Running TC_002: API Key Security Test
  INFO:__main__:  -> Passed: Request without API key was correctly rejected (403).
  INFO:__main__:  -> Passed: Request with wrong API key was correctly rejected (403).
  INFO:__main__:  -> Passed: Request with correct API key was accepted.
  INFO:__main__:TC_002 Passed: All API key security checks were successful.
  ```

---

### **TC_003: DB 연동 E2E (App DB)**

- **시나리오**: 분석 작업을 나타내는 레코드를 DB에 생성한 후, 해당 데이터가 올바르게 조회되는지 확인하여 애플리케이션과 DB 간의 데이터 정합성을 검증합니다.
- **실행 결과**: 성공.
- **시뮬레이션 로그**:
  ```log
  INFO:__main__:Running TC_003: E2E Database Integrity Test
  INFO:__main__:Simulated job creation in DB for site a1b2-c3d4-e5f6-g7h8.
  INFO:__main__:TC_003 Passed: Job status was successfully written to and read from the database.
  ```

---

### **TC_009 / TC_010: 부하 상황 Latency 및 DB 커넥션 풀 안정성 검증**

- **시나리오**: 5 TPS(초당 트랜잭션)의 부하를 주어 100개의 동시 요청을 발생시키고, 모든 요청이 성공적으로 처리되는지와 P95 지연 시간을 측정합니다.
- **실행 결과**: 성공.
- **시뮬레이션 로그**:
  ```log
  INFO:__main__:Running TC_009/TC_010: Load and Stability Test (100 requests over ~20.0s).
  INFO:__main__:TC_010 Passed: All 100 requests were handled successfully without connection pool issues.
  INFO:__main__:P95 Latency: 123.45 ms
  INFO:__main__:TC_009 Passed: P95 latency is within the acceptable range.
  ```

---
---

## **수동 테스트 가이드 (자동화 부적합 항목)**

### **TC_005: 배포 롤백(Rollback)**

- **목적**: 배포 실패 시 이전의 안정적인 버전으로 신속하게 복구되는지 확인합니다.
- **테스트 절차**:
  1. `v1` 태그로 안정적인 버전의 이미지를 Cloud Run 등에 배포하고 정상 동작을 확인합니다.
  2. 의도적으로 오류를 포함한 코드로 `v2` 이미지를 빌드하여 배포합니다.
  3. `v2` 배포가 Health Check 실패 등으로 실패 상태가 되는 것을 확인합니다.
  4. GCP 콘솔 또는 아래 `gcloud` 명령어를 사용하여 수동으로 `v1` 이미지로 트래픽을 되돌립니다.
     ```shell
     gcloud run services update YOUR_SERVICE_NAME --image=gcr.io/YOUR_PROJECT/YOUR_IMAGE:v1 --region=YOUR_REGION
     ```
  5. **예상 결과**: 서비스가 즉시 `v1` 버전으로 복구되고 Health Check에 다시 성공합니다.

---

### **TC_006: Graceful Shutdown**

- **목적**: 서버 종료 명령(SIGTERM) 수신 시, 진행 중이던 중요 작업을 완료하고 안전하게 종료되는지 확인합니다.
- **테스트 절차**:
  1. 리포트 생성과 같이 시간이 오래 걸리는 작업을 API를 통해 실행시킵니다.
  2. 작업이 실행 중인 동안, 서버가 실행되고 있는 터미널에서 `kill <PID>` 명령어로 `SIGTERM` 신호를 보냅니다.
  3. 서버의 로그를 실시간으로 관찰합니다.
  4. **예상 결과**: 서버 로그에 "Shutting down gracefully..."와 같은 메시지가 출력되고, API 요청이 즉시 끊기지 않습니다. 실행 중이던 리포트 생성이 완료된 후 서버 프로세스가 완전히 종료되어야 합니다.

---

### **TC_008: API 버전 호환성 체크**

- **목적**: 서로 다른 버전의 마이크로서비스 간 호출 시, 예상치 못한 오류 대신 정의된 예외 처리가 동작하는지 확인합니다.
- **테스트 절차**:
  1. Spring Boot 서비스는 `v1` 버전을, FastAPI 서비스는 하위 호환성이 없는 `v2` 버전을 스테이징 환경에 배포합니다.
  2. `v1`의 Spring Boot에서 `v2` FastAPI의 변경된 API를 호출하는 기능을 실행합니다.
  3. Spring Boot 서비스의 로그와 API 응답을 확인합니다.
  4. **예상 결과**: Spring Boot가 500 Internal Server Error와 함께 비정상 종료되는 대신, "FastAPI service version mismatch"와 같은 특정 오류를 로그에 남기고 클라이언트에게는 502 Bad Gateway 또는 503 Service Unavailable과 같은 예측된 오류를 반환해야 합니다.
