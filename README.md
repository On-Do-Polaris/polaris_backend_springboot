# SKAX Physical Risk Management API

사업장 기후 물리적 리스크 관리 시스템 백엔드 API (Spring Boot)

## Tech Stack

- **Java**: 21.0.9 LTS
- **Spring Boot**: 3.5.7
- **Spring Framework**: 6.2.12
- **Build Tool**: Maven 3.9.11
- **Database**: MariaDB 11.4.3 LTS
- **Cache**: Redis
- **Storage**: AWS S3
- **External API**: FastAPI (AI Agent)

## Architecture

```
src/main/java/com/skax/physicalrisk/
├── config/              # 설정 클래스
├── controller/          # REST API 컨트롤러
├── service/             # 비즈니스 로직
├── domain/              # 도메인 엔티티 & 레포지토리
├── dto/                 # DTO (Request/Response)
├── security/            # JWT & Spring Security
├── client/              # 외부 API 클라이언트 (FastAPI)
├── exception/           # 예외 처리
└── util/                # 유틸리티
```

## Getting Started

### Prerequisites

- JDK 21
- Maven 3.9+
- MariaDB 11.4+
- Redis

### Installation

1. Clone the repository
```bash
git clone <repository-url>
cd backend_team_java
```

2. Set up environment variables
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. Build the project
```bash
mvn clean install
```

4. Run the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

OpenAPI Specification: `openapi-springboot.yaml`

## Environment Variables

See `.env.example` for all required environment variables.

## License

Proprietary
