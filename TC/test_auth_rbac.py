
import os
import time
import logging
import httpx
import pytest
from dotenv import load_dotenv
import jwt

# --- 로깅 설정 ---
log_file_path = os.path.join(os.path.dirname(__file__), 'test_results.log')
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(log_file_path, mode='w'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# --- 환경 변수 로드 ---
# .env 파일이 있으면 로드하고, 없으면 .env.example을 참고하도록 안내합니다.
if os.path.exists(os.path.join(os.path.dirname(__file__), '.env')):
    load_dotenv(os.path.join(os.path.dirname(__file__), '.env'))
else:
    logger.warning("'.env' file not found. Please create it based on '.env.example' and provide necessary credentials.")
    # 테스트가 비정상 종료되지 않도록 기본값 설정
    os.environ.setdefault('BASE_URL', 'http://localhost:8080')
    os.environ.setdefault('ADMIN_USERNAME', 'admin')
    os.environ.setdefault('ADMIN_PASSWORD', 'password')
    os.environ.setdefault('ESG_MANAGER_USERNAME', 'esg')
    os.environ.setdefault('ESG_MANAGER_PASSWORD', 'password')
    os.environ.setdefault('OTHER_USER_SITE_ID', 'a1b2c3d4-e5f6-7890-1234-567890abcdef')


# --- 테스트 환경 설정 ---
BASE_URL = os.getenv("BASE_URL")
ADMIN_CREDENTIALS = (os.getenv("ADMIN_USERNAME"), os.getenv("ADMIN_PASSWORD"))
ESG_MANAGER_CREDENTIALS = (os.getenv("ESG_MANAGER_USERNAME"), os.getenv("ESG_MANAGER_PASSWORD"))
OTHER_USER_SITE_ID = os.getenv("OTHER_USER_SITE_ID")

# 테스트할 엔드포인트 경로
# 실제 애플리케이션의 엔드포인트에 맞춰 수정해야 할 수 있습니다.
LOGIN_ENDPOINT = "/api/v1/auth/login"
REFRESH_ENDPOINT = "/api/v1/auth/refresh-token"
ADMIN_ONLY_ENDPOINT = "/api/v1/users" # 사용자 목록 조회 (Admin 전용으로 가정)
COMMON_ENDPOINT = "/api/v1/dashboard" # 대시보드 (공통 접근 가능으로 가정)
SITE_DETAIL_ENDPOINT = "/api/v1/sites/{site_id}" # 사업장 상세 조회

# --- Pytest Fixtures ---

@pytest.fixture(scope="session")
def api_client():
    """세션 단위로 재사용되는 httpx 클라이언트 Fixture"""
    with httpx.Client(base_url=BASE_URL, timeout=10.0) as client:
        yield client

def _login_and_get_tokens(client: httpx.Client, username, password):
    """로그인하여 토큰을 반환하는 헬퍼 함수"""
    try:
        response = client.post(LOGIN_ENDPOINT, json={"email": username, "password": password})
        response.raise_for_status()
        data = response.json()
        return data.get("accessToken"), data.get("refreshToken"), data.get("userId")
    except (httpx.HTTPStatusError, httpx.RequestError) as e:
        logger.error(f"Failed to log in as {username}: {e}")
        return None, None, None

@pytest.fixture(scope="session")
def admin_tokens(api_client):
    """Admin 역할의 Access/Refresh 토큰을 제공하는 Fixture"""
    access_token, refresh_token, _ = _login_and_get_tokens(api_client, *ADMIN_CREDENTIALS)
    if not access_token:
        pytest.fail("Could not log in as Admin. Check credentials and server status.", pytrace=False)
    return {"access": access_token, "refresh": refresh_token}

@pytest.fixture(scope="session")
def esg_manager_data(api_client):
    """ESG 담당자 역할의 토큰 및 ID를 제공하는 Fixture"""
    access_token, refresh_token, user_id = _login_and_get_tokens(api_client, *ESG_MANAGER_CREDENTIALS)
    if not access_token:
        pytest.fail("Could not log in as ESG Manager. Check credentials and server status.", pytrace=False)
    return {"access": access_token, "refresh": refresh_token, "userId": user_id}

@pytest.fixture
def expired_access_token(api_client):
    """
    만료된 Access Token을 생성하는 Fixture.
    경고: 이 테스트(TC_005)가 성공하려면, 테스트 실행 시 서버의 Access Token 만료 시간을
    매우 짧게(예: 1000ms) 설정해야 합니다.
    """
    logger.warning("TC_005 requires the server's access token expiration to be set to a very short duration (e.g., 1000ms) to pass.")
    access_token, _, _ = _login_and_get_tokens(api_client, *ADMIN_CREDENTIALS)
    if not access_token:
        pytest.fail("Could not obtain a token to expire.", pytrace=False)
    
    # 토큰 유효 시간보다 길게 대기 (서버 설정에 의존적)
    time.sleep(1.5) # 서버의 토큰 만료가 1초로 설정되었다고 가정
    return access_token


# --- 테스트 케이스 ---

def test_tc001_successful_login(api_client):
    """TC_001: 정상 로그인 (유효한 Admin ID/PW)"""
    logger.info("Running TC_001: Successful Login")
    try:
        response = api_client.post(LOGIN_ENDPOINT, json={"email": ADMIN_CREDENTIALS[0], "password": ADMIN_CREDENTIALS[1]})
        
        assert response.status_code == 200, f"Expected 200 OK, but got {response.status_code}"
        
        data = response.json()
        assert "accessToken" in data and data["accessToken"]
        assert "refreshToken" in data and data["refreshToken"]
        assert "roles" in data and "ADMIN" in data["roles"]
        
        # 민감정보 노출 여부 확인 (예: password 필드가 없어야 함)
        assert "password" not in data
        logger.info("TC_001 Passed: Successfully logged in and received tokens.")

    except Exception as e:
        logger.error(f"TC_001 Failed: {e}", exc_info=True)
        pytest.fail(f"TC_001 failed with exception: {e}")

def test_tc002_wrong_password(api_client):
    """TC_002: 비밀번호 불일치 로그인 시도"""
    logger.info("Running TC_002: Wrong Password")
    response = api_client.post(LOGIN_ENDPOINT, json={"email": ADMIN_CREDENTIALS[0], "password": "wrongpassword"})
    
    assert response.status_code == 401, f"Expected 401 Unauthorized, but got {response.status_code}"
    logger.info("TC_002 Passed: Correctly blocked login with wrong password.")

def test_tc003_insufficient_permissions(api_client, esg_manager_data):
    """TC_003: 권한 부족 접근 (ESG 담당자가 Admin 전용 API 호출)"""
    logger.info("Running TC_003: Insufficient Permissions (ESG Manager trying to access Admin endpoint)")
    headers = {"Authorization": f"Bearer {esg_manager_data['access']}"}
    response = api_client.get(ADMIN_ONLY_ENDPOINT, headers=headers)
    
    assert response.status_code == 403, f"Expected 403 Forbidden, but got {response.status_code}"
    logger.info("TC_003 Passed: Access denied for ESG Manager on Admin endpoint as expected.")

def test_tc004_common_function_access(api_client, admin_tokens, esg_manager_data):
    """TC_004: 공통 기능 접근 (Admin, ESG 담당자 모두 접근 가능)"""
    logger.info("Running TC_004: Common Function Access")
    
    # 1. Admin으로 접근
    headers_admin = {"Authorization": f"Bearer {admin_tokens['access']}"}
    response_admin = api_client.get(COMMON_ENDPOINT, headers=headers_admin)
    assert response_admin.status_code == 200, f"Admin failed to access common endpoint. Expected 200, got {response_admin.status_code}"
    logger.info("Admin successfully accessed the common endpoint.")

    # 2. ESG 담당자로 접근
    headers_esg = {"Authorization": f"Bearer {esg_manager_data['access']}"}
    response_esg = api_client.get(COMMON_ENDPOINT, headers=headers_esg)
    assert response_esg.status_code == 200, f"ESG Manager failed to access common endpoint. Expected 200, got {response_esg.status_code}"
    logger.info("ESG Manager successfully accessed the common endpoint.")
    
    logger.info("TC_004 Passed: Both Admin and ESG Manager accessed common endpoint.")

def test_tc005_expired_token_rejection(api_client, expired_access_token):
    """TC_005: 만료된 토큰 사용 차단"""
    logger.info("Running TC_005: Expired Token Rejection")
    headers = {"Authorization": f"Bearer {expired_access_token}"}
    response = api_client.get(COMMON_ENDPOINT, headers=headers)
    
    assert response.status_code == 401, f"Expected 401 Unauthorized for expired token, but got {response.status_code}"
    
    # 에러 메시지 검증 (서버 응답 형식에 따라 수정 필요)
    error_details = response.json()
    assert "token" in error_details.get("message", "").lower() or "expired" in error_details.get("message", "").lower()
    
    logger.info("TC_005 Passed: Expired token was correctly rejected.")

def test_tc006_data_isolation(api_client, esg_manager_data):
    """TC_006: 데이터 격리 검증 (다른 사용자의 사업장 데이터 접근 시도)"""
    logger.info("Running TC_006: Data Isolation")
    
    headers = {"Authorization": f"Bearer {esg_manager_data['access']}"}
    
    # 자신의 소유가 아닌 사업장 ID로 데이터 조회 시도
    url = SITE_DETAIL_ENDPOINT.format(site_id=OTHER_USER_SITE_ID)
    response = api_client.get(url, headers=headers)
    
    # 권한이 없으므로 403 또는 해당 리소스를 찾을 수 없다는 404가 반환될 수 있음
    assert response.status_code in [403, 404], f"Expected 403 or 404, but got {response.status_code}"
    logger.info(f"TC_006 Passed: Access to other user's site data was correctly blocked with status {response.status_code}.")

def test_tc007_token_refresh(api_client, admin_tokens):
    """TC_007: 유효한 Refresh Token으로 토큰 재발급"""
    logger.info("Running TC_007: Token Refresh")
    
    headers = {"Authorization": f"Bearer {admin_tokens['refresh']}"}
    response = api_client.post(REFRESH_ENDPOINT, headers=headers)
    
    assert response.status_code == 200, f"Expected 200 OK for token refresh, but got {response.status_code}"
    
    data = response.json()
    assert "accessToken" in data and data["accessToken"]
    assert data["accessToken"] != admin_tokens['access'] # 새로 발급된 토큰인지 확인
    
    logger.info("TC_007 Passed: Successfully refreshed access token.")

if __name__ == '__main__':
    # .env 파일이 없으면 경고 메시지를 표시하기 위해 로직을 한번 더 실행합니다.
    if not os.path.exists(os.path.join(os.path.dirname(__file__), '.env')):
         logger.warning("'.env' file not found. Running tests with default placeholder values, which will likely fail.")
         logger.warning("Please create a '.env' file based on '.env.example' with your environment's actual values.")

    pytest.main(['-v', __file__])
