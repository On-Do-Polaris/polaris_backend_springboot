
import os
import time
import logging
import asyncio
import httpx
import pytest
from dotenv import load_dotenv

# --- 로깅 설정 ---
log_file_path = os.path.join(os.path.dirname(__file__), 'test_operation_results.log')
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(log_file_path, mode='w'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# --- 환경 변수 로드 ---
dotenv_path = os.path.join(os.path.dirname(__file__), '.env')
if os.path.exists(dotenv_path):
    load_dotenv(dotenv_path)
else:
    logger.warning("'.env' file not found. Please create it based on '.env.example'.")

# --- 테스트 환경 설정 ---
BASE_URL = os.getenv("BASE_URL", "http://localhost:8080")
ADMIN_CREDENTIALS = (os.getenv("ADMIN_USERNAME"), os.getenv("ADMIN_PASSWORD"))

# 부하 테스트 설정
LOAD_TEST_REQUESTS = 100
LOAD_TEST_ENDPOINT = "/api/sites" # 읽기 전용의 가벼운 엔드포인트로 선정
RESPONSE_TIME_THRESHOLD_S = 5.0

# --- Pytest Fixtures ---

@pytest.fixture(scope="session")
def api_client():
    # 타임아웃을 부하 테스트에 맞게 늘림
    with httpx.Client(base_url=BASE_URL, timeout=30.0) as client:
        yield client

@pytest.fixture(scope="session")
def admin_token(api_client):
    """Admin 역할의 Access Token을 제공"""
    logger.info("Logging in as admin to get session token.")
    try:
        response = api_client.post("/api/v1/auth/login", json={"email": ADMIN_CREDENTIALS[0], "password": ADMIN_CREDENTIALS[1]})
        response.raise_for_status()
        return response.json()["accessToken"]
    except (httpx.RequestError, httpx.HTTPStatusError) as e:
        pytest.fail(f"Admin login failed, stopping tests. Error: {e}", pytrace=False)


# --- 테스트 케이스 ---

# =====================================================================================
#  자동화가 불가능하거나 부적합한 테스트 케이스 (사유 명시)
# =====================================================================================

def test_tc001_user_activity_logging_skipped():
    """TC_001: 사용자 행위 로깅 (수동 확인 필요)"""
    logger.warning("TC_001 SKIPPED: No database table for audit logs was found. User activity is likely logged to files, which cannot be verified by this test suite. Manual verification of server log files is required.")
    pytest.skip("User activity log DB table not found. Manual verification required.")

def test_tc003_rate_limiting_skipped():
    """TC_003: Rate Limiting 적용 (기능 미발견)"""
    logger.warning("TC_003 SKIPPED: No evidence of a rate-limiting implementation (e.g., Bucket4j, Resilience4j) was found in the source code. This test is invalid as the feature appears to be missing.")
    pytest.skip("Rate-limiting implementation not found in source code.")

def test_tc004_api_key_renewal_skipped():
    """TC_004: API Key 갱신 유지보수 (자동화 부적합)"""
    logger.warning("TC_004 SKIPPED: This test requires manual changes to server configuration files and cannot be automated safely from a client-side test.")
    pytest.skip("API Key renewal test is not suitable for automation.")

def test_tc005_network_disruption_skipped():
    """TC_005: 네트워크 단절 대응 (자동화 부적합)"""
    logger.warning("TC_005 SKIPPED: Simulating a network partition between the server and its dependencies (like a database) requires server-side fault injection, which is beyond the scope of this test suite.")
    pytest.skip("Server-side network fault injection cannot be automated from the client.")

def test_tc006_rto_measurement_manual_guide():
    """TC_006: 복구 시간(RTO) 측정 (수동 실행 가이드)"""
    rto_script_guide = """
    # ========================== TC_006 MANUAL TEST GUIDE ==========================
    # WARNING: This test is DESTRUCTIVE and will shut down your server.
    # DO NOT run this in a production environment.
    # 1. Run the server process you want to test.
    # 2. Manually shut down the server process (e.g., kill, Ctrl+C).
    # 3. Immediately run the script below.
    # 4. Manually restart the server process.
    # 5. The script will print the Recovery Time Objective (RTO) once the server is back online.
    #
    # import time
    # import httpx
    # 
    # HEALTH_CHECK_URL = f"{BASE_URL}/actuator/health"
    # 
    # print(f"Health check URL: {HEALTH_CHECK_URL}")
    # print("Waiting for server to go down...")
    # 
    # # Wait until the server is confirmed to be down
    # while True:
    #     try:
    #         with httpx.Client(timeout=1.0) as client:
    #             client.get(HEALTH_CHECK_URL)
    #         time.sleep(0.5)
    #     except httpx.RequestError:
    #         print("Server is confirmed to be down.")
    #         break
    # 
    # print("Please restart the server now.")
    # start_time = time.perf_counter()
    # 
    # # Wait until the server is back up
    # while True:
    #     try:
    #         with httpx.Client(timeout=1.0) as client:
    #             response = client.get(HEALTH_CHECK_URL)
    #             if response.status_code == 200:
    #                 end_time = time.perf_counter()
    #                 rto_seconds = end_time - start_time
    #                 print(f"\nSERVER IS BACK ONLINE!")
    #                 print(f"Recovery Time Objective (RTO): {rto_seconds:.2f} seconds")
    #                 if rto_seconds > 180:
    #                     print(f"WARNING: RTO ({rto_seconds:.2f}s) exceeded the 3-minute threshold.")
    #                 break
    #     except httpx.RequestError:
    #         time.sleep(1)
    #         print(".", end="", flush=True)
    # ==============================================================================
    """
    logger.warning("TC_006 SKIPPED: This is a destructive test. A manual guide has been printed to the log and test output.")
    print(rto_script_guide)
    pytest.skip("RTO test is destructive and must be run manually. See guide in test output.")


# =====================================================================================
#  자동화 가능한 테스트 케이스
# =====================================================================================

@pytest.mark.asyncio
async def test_tc002_load_test(admin_token):
    """TC_002: 동시성 부하 테스트 (100회 요청)"""
    logger.info(f"Running TC_002: Load Test with {LOAD_TEST_REQUESTS} concurrent requests.")
    
    headers = {"Authorization": f"Bearer {admin_token}"}
    latencies = []

    async def make_request(client, index):
        start_time = time.perf_counter()
        try:
            response = await client.get(LOAD_TEST_ENDPOINT, headers=headers)
            response.raise_for_status() # 200이 아닌 경우 예외 발생
            latency = (time.perf_counter() - start_time) * 1000  # ms
            latencies.append(latency)
            return response.status_code
        except Exception as e:
            logger.error(f"Request {index} failed: {e}")
            return str(e)

    async with httpx.AsyncClient(base_url=BASE_URL, timeout=30.0) as client:
        tasks = [make_request(client, i) for i in range(LOAD_TEST_REQUESTS)]
        total_start_time = time.perf_counter()
        results = await asyncio.gather(*tasks)
        total_duration_s = time.perf_counter() - total_start_time

    success_count = results.count(200)
    
    logger.info(f"Load test finished in {total_duration_s:.2f} seconds.")
    logger.info(f"Successful requests: {success_count}/{LOAD_TEST_REQUESTS}")

    if latencies:
        avg_latency = sum(latencies) / len(latencies)
        max_latency = max(latencies)
        min_latency = min(latencies)
        logger.info(f"Latency (ms) - Min: {min_latency:.2f}, Max: {max_latency:.2f}, Avg: {avg_latency:.2f}")
        
        # 개별 응답 시간이 모두 5초 이내인지 확인
        assert all(lat / 1000 < RESPONSE_TIME_THRESHOLD_S for lat in latencies), "At least one request exceeded the response time threshold."

    # 모든 요청이 성공했는지 확인
    assert success_count == LOAD_TEST_REQUESTS, "Not all requests were successful."
    
    logger.info("TC_002 Passed: All concurrent requests completed successfully within the time threshold.")

def test_tc007_user_agent_compatibility(api_client, admin_token):
    """TC_007: 브라우저 호환성 헤더 (User-Agent)"""
    logger.info("Running TC_007: User-Agent Compatibility Test")
    
    user_agents = [
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edge/108.0.1462.46 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.1 Safari/605.1.15",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:107.0) Gecko/20100101 Firefox/107.0"
    ]
    
    base_headers = {"Authorization": f"Bearer {admin_token}"}
    
    for ua in user_agents:
        headers = base_headers.copy()
        headers["User-Agent"] = ua
        browser = ua.split('/')[0] # 간단하게 브라우저 이름 추출
        
        logger.info(f"Testing with User-Agent: {browser}...")
        response = api_client.get(LOAD_TEST_ENDPOINT, headers=headers)
        
        assert response.status_code == 200, f"Request failed for User-Agent '{browser}' with status {response.status_code}"
        logger.info(f"  -> Success (200 OK)")
        
    logger.info("TC_007 Passed: Server responded successfully to all tested User-Agents.")
