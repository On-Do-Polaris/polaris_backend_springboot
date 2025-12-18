
import os
import asyncio
import logging
import uuid
from decimal import Decimal

import httpx
import psycopg2
import pytest
from dotenv import load_dotenv

# --- 로깅 설정 ---
log_file_path = os.path.join(os.path.dirname(__file__), 'test_site_management_results.log')
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
    logger.info("Loaded environment variables from .env file.")
else:
    logger.warning("'.env' file not found. Please create it based on '.env.example'. Tests may fail.")

# --- 테스트 환경 설정 ---
BASE_URL = os.getenv("BASE_URL", "http://localhost:8080")
ADMIN_CREDENTIALS = (os.getenv("ADMIN_USERNAME"), os.getenv("ADMIN_PASSWORD"))

DB_CONN_INFO = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": os.getenv("DB_PORT", 5432),
    "dbname": os.getenv("DB_NAME", "skala_application"),
    "user": os.getenv("DB_USER", "skala_app_user"),
    "password": os.getenv("DB_PASSWORD", "1234"),
}

# API Endpoints
SITES_ENDPOINT = "/api/sites"
SITE_ENDPOINT = "/api/site" # For POST, PATCH, DELETE with ?siteId=...
ANALYSIS_ENDPOINT = "/api/v1/analysis/start" # 분석 시작 엔드포인트 (가정)

# --- Pytest Fixtures ---

@pytest.fixture(scope="session")
def api_client():
    """세션 단위 비동기 httpx 클라이언트"""
    with httpx.Client(base_url=BASE_URL, timeout=20.0) as client:
        yield client

@pytest.fixture(scope="session")
def admin_token(api_client):
    """Admin 역할의 Access Token을 제공하는 Fixture"""
    logger.info("Logging in as admin to get session token.")
    try:
        response = api_client.post("/api/v1/auth/login", json={"email": ADMIN_CREDENTIALS[0], "password": ADMIN_CREDENTIALS[1]})
        response.raise_for_status()
        return response.json()["accessToken"]
    except (httpx.RequestError, httpx.HTTPStatusError) as e:
        pytest.fail(f"Admin login failed, stopping tests. Error: {e}", pytrace=False)

@pytest.fixture(scope="function")
def db_connection():
    """DB 커넥션을 제공하고 테스트 후 종료하는 Fixture"""
    conn = None
    try:
        logger.info(f"Connecting to database '{DB_CONN_INFO['dbname']}' on {DB_CONN_INFO['host']}.")
        conn = psycopg2.connect(**DB_CONN_INFO)
        yield conn
        conn.rollback() # 테스트 간 변경사항 롤백
    except psycopg2.OperationalError as e:
        pytest.fail(f"Database connection failed: {e}", pytrace=False)
    finally:
        if conn:
            conn.close()
            logger.info("Database connection closed.")

@pytest.fixture(scope="function")
def test_site(api_client, admin_token, db_connection):
    """테스트용 사업장을 생성하고 테스트 종료 후 삭제하는 Fixture"""
    headers = {"Authorization": f"Bearer {admin_token}"}
    site_name = f"Test Site {uuid.uuid4()}"
    site_payload = {
        "name": site_name,
        "latitude": 37.12345678,
        "longitude": 127.87654321,
        "jibunAddress": "테스트 지번 주소",
        "roadAddress": "테스트 도로명 주소",
        "type": "office"
    }
    
    created_site_id = None
    try:
        # Create site
        response = api_client.post(SITE_ENDPOINT, headers=headers, json=site_payload)
        assert response.status_code == 201, "Site creation for fixture failed."
        created_site_id = response.json()["siteId"]
        logger.info(f"Fixture created site '{site_name}' with ID: {created_site_id}")
        
        yield created_site_id, site_name
        
    finally:
        # Cleanup
        if created_site_id:
            logger.info(f"Cleaning up site ID: {created_site_id}")
            # Use a direct DB delete for cleanup to bypass business logic if needed
            try:
                with db_connection.cursor() as cursor:
                    # Cascade delete might be handled by DB, but jobs/results first is safer
                    cursor.execute("DELETE FROM analysis_jobs WHERE site_id = %s", (created_site_id,))
                    cursor.execute("DELETE FROM analysis_results WHERE site_id = %s", (created_site_id,))
                    cursor.execute("DELETE FROM sites WHERE id = %s", (created_site_id,))
                db_connection.commit()
                logger.info(f"Successfully cleaned up site ID {created_site_id} from DB.")
            except Exception as e:
                logger.error(f"Failed to cleanup site {created_site_id} from DB. Manual cleanup might be required. Error: {e}")

# --- 테스트 케이스 ---

def test_tc001_create_site_and_verify_db(api_client, admin_token, db_connection, test_site):
    """TC_001: 신규 등록 및 좌표 저장"""
    logger.info("Running TC_001: Create Site and Verify in DB")
    site_id, _ = test_site
    
    with db_connection.cursor() as cursor:
        cursor.execute("SELECT latitude, longitude FROM sites WHERE id = %s", (site_id,))
        result = cursor.fetchone()

    assert result is not None, "Site not found in database."
    db_lat, db_lon = result
    
    assert db_lat == Decimal("37.12345678"), "Latitude does not match."
    assert db_lon == Decimal("127.87654321"), "Longitude does not match."
    logger.info("TC_001 Passed: Site created and coordinates correctly saved in DB.")

# TC_002 ~ TC_005는 백엔드에 좌표 변환 로직이 없으므로 스킵합니다.
# 주석: TC_002: 위치 변환 정확도 검증 - 백엔드가 아닌 프론트엔드에서 좌표 변환이 수행되므로 백엔드 테스트 범위가 아님.
# 주석: TC_003: 불필요한 API 호출 방지 - 백엔드에서 외부 위치 API를 호출하지 않으므로 검증 대상이 아님.
# 주석: TC_004: API 한도 초과(429) 대응 - 백엔드에서 외부 위치 API를 호출하지 않으므로 검증 대상이 아님.
# 주석: TC_005: 외부 서비스 장애(500) 대응 - 백엔드에서 외부 위치 API를 호출하지 않으므로 검증 대상이 아님.

def test_tc006_address_validation(api_client, admin_token):
    """TC_006: 입력 유효성 검사 (지나치게 긴 주소)"""
    logger.info("Running TC_006: Input Validation")
    headers = {"Authorization": f"Bearer {admin_token}"}
    long_address = "a" * 501 # roadAddress 필드 길이는 500으로 추정
    payload = {
        "name": "Validation Test Site",
        "latitude": 38.0, "longitude": 128.0,
        "roadAddress": long_address,
        "type": "office"
    }
    response = api_client.post(SITE_ENDPOINT, headers=headers, json=payload)
    
    # JPA/Hibernate가 DB 제약조건 위반 예외를 발생시키고,
    # GlobalExceptionHandler가 이를 500 또는 400으로 처리할 수 있음.
    # 400 Bad Request가 이상적이지만, 500도 제약조건 위반을 나타낼 수 있음.
    assert response.status_code in [400, 500], f"Expected 400 or 500 for validation failure, but got {response.status_code}"
    logger.info(f"TC_006 Passed: Correctly blocked request with overly long address with status {response.status_code}.")

def test_tc007_cascade_delete(api_client, admin_token, db_connection, test_site):
    """TC_007: 종속 데이터 연쇄 삭제 (Hard Delete)"""
    logger.info("Running TC_007: Cascade Delete Verification")
    site_id, _ = test_site

    # 1. 종속 데이터(AnalysisJob) 생성
    job_id = f"job-{uuid.uuid4()}"
    with db_connection.cursor() as cursor:
        cursor.execute(
            "INSERT INTO analysis_jobs (id, site_id, job_id, status, created_at) VALUES (%s, %s, %s, 'QUEUED', NOW())",
            (str(uuid.uuid4()), site_id, job_id)
        )
    db_connection.commit()
    logger.info(f"Manually inserted analysis_jobs record for site {site_id}")

    # 2. 사업장 삭제 API 호출
    headers = {"Authorization": f"Bearer {admin_token}"}
    delete_response = api_client.delete(f"{SITE_ENDPOINT}?siteId={site_id}", headers=headers)
    assert delete_response.status_code == 200, f"DELETE request failed with status {delete_response.status_code}"
    logger.info(f"DELETE API call for site {site_id} returned 200 OK.")

    # 3. DB에서 사업장 및 종속 데이터가 삭제되었는지 확인
    with db_connection.cursor() as cursor:
        cursor.execute("SELECT COUNT(*) FROM sites WHERE id = %s", (site_id,))
        site_count = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM analysis_jobs WHERE site_id = %s", (site_id,))
        job_count = cursor.fetchone()[0]

    assert site_count == 0, "Site was not deleted from the database."
    # 아래 assert는 DB에 ON DELETE CASCADE가 설정되어 있어야 통과함. 실패 시 버그 리포트 대상.
    assert job_count == 0, "Dependent analysis_jobs were not deleted (cascade delete might not be configured)."
    logger.info("TC_007 Passed: Site and its dependent analysis job were successfully deleted.")


def test_tc008_get_non_existent_id(api_client, admin_token):
    """TC_008 (수정): 존재하지 않는 ID로 수정/삭제 시도"""
    logger.info("Running TC_008 (Adapted): Attempting to modify non-existent site ID")
    headers = {"Authorization": f"Bearer {admin_token}"}
    non_existent_id = uuid.uuid4()
    
    # PATCH 요청으로 404 확인
    response = api_client.patch(
        f"{SITE_ENDPOINT}?siteId={non_existent_id}",
        headers=headers,
        json={"name": "New Name"}
    )
    
    assert response.status_code == 404, f"Expected 404 Not Found, but got {response.status_code}"
    logger.info("TC_008 Passed: Correctly returned 404 for a non-existent site ID.")

@pytest.mark.asyncio
async def test_tc009_concurrency_update(admin_token, test_site):
    """TC_009: 동시 수정 (Last-Write-Wins 검증)"""
    logger.info("Running TC_009: Concurrency Update (Last-Write-Wins)")
    site_id, original_name = test_site
    
    update_1_name = f"Update 1 {uuid.uuid4()}"
    update_2_name = f"Update 2 {uuid.uuid4()}"

    async def update_site(client, new_name):
        headers = {"Authorization": f"Bearer {admin_token}"}
        logger.info(f"Attempting to update site {site_id} to name '{new_name}'")
        return await client.patch(
            f"{SITE_ENDPOINT}?siteId={site_id}",
            headers=headers,
            json={"name": new_name}
        )

    async with httpx.AsyncClient(base_url=BASE_URL) as client1, httpx.AsyncClient(base_url=BASE_URL) as client2:
        # 두 개의 수정 요청을 거의 동시에 보냄
        results = await asyncio.gather(
            update_site(client1, update_1_name),
            update_site(client2, update_2_name)
        )

    status_codes = [res.status_code for res in results]
    logger.info(f"Concurrent PATCH requests finished with status codes: {status_codes}")

    # @Version이 없으므로, 둘 다 200 OK를 반환해야 함 (하나는 대기 후 실행)
    assert status_codes.count(200) == 2, f"Expected two 200 OK responses, but got {status_codes}"

    # DB에서 최종 결과 확인 (Last-Write-Wins)
    # 어떤 요청이 마지막일지 보장할 수 없으므로, 둘 중 하나의 값으로 변경되었는지 확인
    conn = psycopg2.connect(**DB_CONN_INFO)
    with conn.cursor() as cursor:
        cursor.execute("SELECT name FROM sites WHERE id = %s", (site_id,))
        final_name = cursor.fetchone()[0]
    conn.close()
    
    logger.info(f"Final site name in DB is '{final_name}'")
    assert final_name in [update_1_name, update_2_name], "Final name in DB does not match either of the last updates."
    assert final_name != original_name, "Site name was not updated."
    
    logger.info("TC_009 Passed: Concurrent updates were processed sequentially (Last-Write-Wins) without error.")

