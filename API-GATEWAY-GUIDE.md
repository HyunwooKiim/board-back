# API Gateway 사용 가이드

## 개요

이제 프론트엔드는 **단일 엔드포인트(http://localhost:8080)** 로만 요청을 보내면 됩니다!
API Gateway가 자동으로 각 마이크로서비스로 요청을 라우팅합니다.

## 변경 사항

### 1. 데이터베이스: H2 → MySQL
- **MySQL 8.0** 사용
- **JPA가 자동으로 데이터베이스 생성** (`createDatabaseIfNotExist=true`)
- 각 서비스별 독립된 데이터베이스:
  - `userdb` (User Service)
  - `articledb` (Article Service)
  - `commentdb` (Comment Service)
- 수동 SQL 스크립트 불필요 - JPA가 테이블 자동 생성 (`ddl-auto: update`)

### 2. API Gateway 추가
- **포트: 8080**
- 모든 요청을 단일 진입점으로 처리
- 자동 라우팅 및 로드밸런싱

## API 엔드포인트 매핑

### Before (기존 방식)
```
User Service:    http://localhost:8081/...
Article Service: http://localhost:8082/...
Comment Service: http://localhost:8083/...
```

### After (API Gateway 사용)
```
모든 요청: http://localhost:8080/api/...
```

## 라우팅 규칙

| 프론트엔드 요청 | Gateway 라우팅 대상 | 실제 서비스 |
|----------------|---------------------|------------|
| `GET /api/users/...` | → `http://user-service:8081/...` | User Service |
| `GET /api/articles/...` | → `http://article-service:8082/...` | Article Service |
| `GET /api/comments/...` | → `http://comment-service:8083/...` | Comment Service |

## 사용 예시

### 사용자 등록
```bash
# Before
POST http://localhost:8081/register

# After
POST http://localhost:8080/api/users/register
```

### 게시글 조회
```bash
# Before
GET http://localhost:8082/articles

# After
GET http://localhost:8080/api/articles/articles
```

### 댓글 작성
```bash
# Before
POST http://localhost:8083/comments

# After
POST http://localhost:8080/api/comments/comments
```

## 실행 방법

### Docker Compose로 실행 (권장)
```bash
# 빌드
./gradlew clean build

# Docker 실행
docker-compose up --build -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

### 로컬 개발 모드
```bash
# MySQL 먼저 실행
docker run -d --name mysql \
  -e MYSQL_ROOT_PASSWORD=root1234 \
  -p 3306:3306 \
  mysql:8.0

# 각 서비스 실행 (JPA가 자동으로 DB 생성)
./gradlew :service:user:bootRun
./gradlew :service:article:bootRun
./gradlew :service:comment:bootRun
./gradlew :gateway:bootRun
```

## 서비스 포트

| 서비스 | 포트 | 외부 접근 |
|--------|------|----------|
| API Gateway | 8080 | ✅ 프론트엔드 사용 |
| User Service | 8081 | ❌ 내부 통신만 |
| Article Service | 8082 | ❌ 내부 통신만 |
| Comment Service | 8083 | ❌ 내부 통신만 |
| MySQL | 3306 | ⚙️ DB 관리용 |

## CORS 설정

API Gateway에서 CORS를 자동으로 처리합니다:
- 모든 오리진 허용 (`*`)
- 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE, PATCH, OPTIONS)
- 모든 헤더 허용

## 프론트엔드 수정 사항

### React/Vue/Angular 예시
```javascript
// Before
const USER_API = 'http://localhost:8081';
const ARTICLE_API = 'http://localhost:8082';
const COMMENT_API = 'http://localhost:8083';

// After - 하나만 사용!
const API_BASE_URL = 'http://localhost:8080/api';

// 사용 예시
fetch(`${API_BASE_URL}/users/login`, {...})
fetch(`${API_BASE_URL}/articles/1`, {...})
fetch(`${API_BASE_URL}/comments/`, {...})
```

## 트러블슈팅

### MySQL 연결 실패
```bash
# MySQL 컨테이너 상태 확인
docker-compose ps mysql

# MySQL 로그 확인
docker-compose logs mysql

# MySQL 재시작
docker-compose restart mysql
```

### Gateway 라우팅 문제
```bash
# Gateway 로그에서 라우팅 확인
docker-compose logs api-gateway | grep "Route"
```

### 데이터베이스 초기화
```bash
# 모든 컨테이너와 볼륨 삭제 후 재시작
docker-compose down -v
docker-compose up --build -d
```

### Gradle 의존성 문제
```bash
# Gradle 의존성 새로고침
./gradlew clean build --refresh-dependencies
```

## 주의사항

1. **프론트엔드는 8080 포트만 사용하세요**
   - 8081, 8082, 8083은 내부 통신용입니다

2. **경로에 `/api` 접두사 필수**
   - ✅ `/api/users/login`
   - ❌ `/users/login`

3. **Docker 환경에서는 자동으로 MySQL 사용**
   - 로컬 개발시에는 localhost:3306 사용

4. **데이터는 영구 저장됩니다**
   - MySQL 볼륨: `mysql-data`
   - 삭제하려면: `docker-compose down -v`

5. **데이터베이스는 JPA가 자동 생성**
   - 별도 SQL 스크립트 실행 불필요
   - `hibernate.ddl-auto: update` 설정으로 자동 관리
