# API 명세서 및 개발 규칙 (API Specification & Development Rules)

## 1. 개발 원칙 및 코딩 컨벤션 (Development Rules)

본 프로젝트는 유지보수성, 확장성, 그리고 고성능을 지향하며 아래의 핵심 원칙들을 **반드시 준수**해야 합니다.

### 1.1 핵심 설계 원칙

- **SOLID 원칙 준수**:
  - **SRP (단일 책임 원칙)**: 클래스와 메서드는 하나의 책임만 가져야 합니다.
  - **OCP (개방-폐쇄 원칙)**: 확장에는 열려 있고, 수정에는 닫혀 있어야 합니다. (인터페이스 활용)
  - **LSP (리스코프 치환 원칙)**: 자식 클래스는 부모 클래스의 역할을 대체할 수 있어야 합니다.
  - **ISP (인터페이스 분리 원칙)**: 범용 인터페이스 하나보다 구체적인 여러 개의 인터페이스가 낫습니다.
  - **DIP (의존 역전 원칙)**: 구체화가 아닌 추상화에 의존해야 합니다.
- **객체지향 생활체조 원칙 (Object Calisthenics)**:
  - 한 메서드에 오직 한 단계의 들여쓰기(indent)만 허용합니다.
  - `else` 예약어를 쓰지 않습니다. (Early Return 패턴 사용)
  - 모든 원시값과 문자열을 포장(Wrap)합니다. (VO 패턴 활용 권장)
  - 한 줄에 점(.)을 하나만 찍습니다. (디미터 법칙 준수)
  - 줄여쓰지 않습니다. (명확한 네이밍)
  - 모든 엔티티를 작게 유지합니다.
  - 2개 이상의 인스턴스 변수를 가진 클래스를 쓰지 않습니다. (높은 응집도 지향)
  - 일급 컬렉션을 사용합니다.
  - Getter/Setter/Property를 쓰지 않습니다. (객체에 메시지를 보내라)

### 1.2 코드 품질 및 효율성

- **DRY (Don't Repeat Yourself)**: 중복 코드를 발견하면 즉시 리팩토링하여 공통화합니다.
- **KISS (Keep It Simple, Stupid)**: 불필요한 복잡성을 피하고, 코드를 단순하고 읽기 쉽게 유지합니다.
- **YAGNI (You Ain't Gonna Need It)**: 당장 필요하지 않은 기능이나 추상화는 미리 구현하지 않습니다.

### 1.3 성능 최적화 및 오버헤드 방지

- **N+1 문제 방지 (Critical)**:
  - JPA 연관 관계 조회 시 `Fetch Join`, `EntityGraph`, 또는 `BatchSize`를 적절히 사용하여 N+1 쿼리 발생을 원천 차단합니다.
  - 연관된 엔티티를 루프 돌며 조회하는 코드는 절대 금지합니다.
- **불필요한 객체 생성 방지**:
  - 무거운 객체는 싱글톤 빈으로 관리하거나 캐싱하여 재사용합니다.
  - 반복문 내에서의 객체 생성은 신중하게 결정합니다.
- **쿼리 최적화**:
  - 필요한 컬럼만 조회(Projection)하거나, 인덱스를 탈 수 있도록 쿼리를 작성합니다.
  - 대용량 데이터 조회 시 `Offset` 기반 페이징 대신 `Cursor` 기반 페이징(No-Offset)을 우선 고려합니다.

---

## 2. API 명세 (API Specification)

모든 API 응답은 표준 포맷(`ApiResponse<T>`)을 따르며, 에러 발생 시 정해진 에러 코드와 메시지를 반환합니다.

### 2.1 공통 응답 구조

```json
{
  "success": true,
  "code": 200,
  "message": "요청 성공",
  "data": { ... }
}
```

### 2.2 인증 (Auth)

| Method | Endpoint | 설명 | Request Body | Response Data |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/v1/auth/signup` | 회원가입 | `{ "username": "...", "password": "...", "nickname": "..." }` | `{ "userId": 1 }` |
| `POST` | `/v1/auth/login` | 로그인 | `{ "username": "...", "password": "..." }` | `{ "accessToken": "...", "refreshToken": "..." }` |
| `POST` | `/v1/auth/logout` | 로그아웃 | - | - |

### 2.3 게시글 (Article)

| Method | Endpoint | 설명 | Request Body | Response Data |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/v1/articles/{articleId}` | 게시글 단건 조회 | - | `{ "articleId": 1, "title": "...", "content": "...", ... }` |
| `GET` | `/v1/articles` | 게시글 목록 조회 (무한 스크롤) | `Query Params: boardId, pageSize, lastArticleId` | `[ { "articleId": 1, ... }, ... ]` |
| `POST` | `/v1/articles` | 게시글 작성 | `{ "boardId": 1, "title": "...", "content": "..." }` | `{ "articleId": 1 }` |
| `PUT` | `/v1/articles/{articleId}` | 게시글 수정 | `{ "title": "...", "content": "..." }` | `{ "articleId": 1 }` |
| `DELETE` | `/v1/articles/{articleId}` | 게시글 삭제 | - | - |

### 2.4 댓글 (Comment)

| Method | Endpoint | 설명 | Request Body | Response Data |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/v1/comments` | 댓글 작성 | `{ "articleId": 1, "content": "...", "parentId": null }` | `{ "commentId": 1 }` |
| `GET` | `/v1/comments` | 댓글 목록 조회 | `Query Params: articleId, pageSize, lastCommentId` | `[ { "commentId": 1, "content": "...", "children": [...] }, ... ]` |
| `DELETE` | `/v1/comments/{commentId}` | 댓글 삭제 | - | - |
