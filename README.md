# Spring Boot 게시판 프로젝트 (Board Project)

## 1. 개선 목표 (Improvement Goals)

### 1. 글로벌 예외 처리 및 표준 응답 포맷 도입

- **문제점**: 기존 코드는 예외 발생 시 일관성 없는 응답을 반환하거나, 스택 트레이스를 그대로 노출하는 보안 취약점이 있었습니다. 또한, 성공/실패 응답 구조가 제각각이라 프론트엔드 연동 시 혼란을 야기했습니다.
- **해결 방안**: `GlobalExceptionHandler`를 도입하여 모든 예외를 중앙에서 처리하고, `ApiResponse<T>` 표준 포맷을 정의하여 성공/실패 여부, 상태 코드, 메시지, 데이터를 일관된 구조로 반환하도록 개선했습니다.
- **기대 효과**: API 사용성이 대폭 향상되었으며, 예외 상황에 대한 예측 가능성이 높아져 유지보수가 용이해졌습니다.

### 2. Snowflake ID 생성기 아키텍처 개선 (Singleton & Configuration)

- **문제점**: Auto Increment ID는 DB에 의존적이며, 분산 환경에서 ID 충돌이나 예측 가능한 ID로 인한 보안 문제(ID Enumeration Attack)가 발생할 수 있습니다. UUID는 인덱싱 성능이 떨어지는 단점이 있습니다.
- **해결 방안**: Twitter Snowflake 알고리즘을 기반으로 하는 `SnowflakeIdGenerator`를 직접 구현하고, 이를 Spring Bean(Singleton)으로 등록하여 애플리케이션 전역에서 고유하고 시간 순으로 정렬 가능한 64비트 ID를 생성하도록 했습니다. `application.yml`을 통해 Datacenter ID와 Worker ID를 설정할 수 있게 하여 확장성을 확보했습니다.
- **기대 효과**: DB 독립적인 ID 생성이 가능해졌으며, 시간 순 정렬이 보장되어 인덱싱 성능 저하 없이 분산 환경에서도 안전하게 사용할 수 있습니다.

---

## 2. 프로젝트 개요

- **프로젝트명**: GC Board (Global Standard Board)
- **개발 인원: 1인**
- **배포 URL: [실제 배포 주소]**
- **개발 기간**: 2024.12.01 ~ 2024.12.07
- **주요 기능**:
  - 사용자 인증 (회원가입, 로그인, JWT 토큰 발급)
  - 게시글 CRUD (작성, 조회, 수정, 삭제)
  - 댓글 CRUD (작성, 조회, 삭제, 대댓글 지원)
  - 커서 기반 무한 스크롤 (성능 최적화)

---

## 3. 기술 스택 (Tech Stack)

- **Language**: Java 21
- **Framework**: Spring Boot 3.3.0
- **Database**: H2 (Development), MySQL (Production)
- **ORM**: JPA (Hibernate)
- **Security**: JWT (JSON Web Token)
- **Build Tool**: Gradle
- **Architecture**: Layered Architecture (Controller, Service, Repository)

---

## 4. 심화 기술 적용 (Advanced Features)

### ✅ 커서 기반 무한 스크롤 (Cursor-based Pagination)

- **선택 이유**: 일반적인 오프셋 기반 페이징(`OFFSET N LIMIT M`)은 데이터가 많아질수록 앞부분을 스킵하는 비용이 커져 성능이 저하됩니다. 또한, 페이징 도중 데이터가 추가/삭제되면 중복되거나 누락되는 데이터가 발생할 수 있습니다.
- **구현 방식**: 마지막으로 조회한 게시글의 ID(`lastArticleId`)를 커서로 사용하여, 그보다 작은 ID를 가진 게시글을 조회하는 방식(`WHERE id < :lastArticleId`)을 적용했습니다.
- **기술적 이점**: 테이블 풀 스캔 없이 인덱스를 타고 조회하므로 데이터 양과 무관하게 일정한 조회 성능을 보장합니다.
- **관련 코드**: `ArticleRepository.findAllByBoardIdAndIdLessThan`

### ✅ 계층형 댓글 (Hierarchical Comments)

- **선택 이유**: 단순한 댓글 목록이 아닌, 대댓글(답글) 기능을 통해 사용자 간의 소통을 강화하고자 했습니다.
- **구현 방식**: `Comment` 엔티티에 `parent` 필드를 두어 자기 참조 관계를 맺고, 조회 시 메모리 상에서 트리 구조로 변환하여 반환하는 방식을 사용했습니다.
- **관련 코드**: `CommentService.convertToHierarchy`

---

## 5. API 명세 (API Specification)

### 5.1 인증 (Auth)

| Method | Endpoint | 설명 |
| :--- | :--- | :--- |
| `POST` | `/v1/auth/signup` | 회원가입 |
| `POST` | `/v1/auth/login` | 로그인 (JWT 발급) |

### 5.2 게시글 (Article)

| Method | Endpoint | 설명 |
| :--- | :--- | :--- |
| `GET` | `/v1/articles/{articleId}` | 게시글 단건 조회 |
| `GET` | `/v1/articles` | 게시글 목록 조회 (Cursor Pagination) |
| `POST` | `/v1/articles` | 게시글 작성 |
| `PUT` | `/v1/articles/{articleId}` | 게시글 수정 |
| `DELETE` | `/v1/articles/{articleId}` | 게시글 삭제 |

### 5.3 댓글 (Comment)

| Method | Endpoint | 설명 |
| :--- | :--- | :--- |
| `POST` | `/v1/comments` | 댓글 작성 (대댓글 가능) |
| `GET` | `/v1/comments` | 댓글 목록 조회 (계층형 구조) |
| `DELETE` | `/v1/comments/{commentId}` | 댓글 삭제 |

---

## 6. 로컬 실행 방법 (How to Run)

1. **Repository Clone**

   ```bash
   git clone https://github.com/your-repo/board.git
   cd board
   ```

2. **Backend 실행**

   ```bash
   ./gradlew clean bootRun
   ```

   - 기본적으로 H2 Database가 인메모리로 실행됩니다.
   - H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:board`)

3. **API 테스트**
   - Postman 또는 Curl을 사용하여 `http://localhost:8080/v1/...` 엔드포인트로 요청을 보냅니다.

---

## 7. 커밋 전략 (Commit Strategy)

- **Atomic Commits**: 기능 단위로 최대한 작게 쪼개어 커밋하여, 문제 발생 시 추적과 롤백이 용이하도록 했습니다.
- **Convention**: `feat`, `fix`, `refactor`, `docs`, `chore` 등의 접두어를 사용하여 커밋의 성격을 명확히 했습니다.
  - 예: `feat: 게시글 커서 페이징 조회 기능 구현`, `refactor: Snowflake ID 생성기 Singleton 패턴 적용`
