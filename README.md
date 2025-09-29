# 📑 월부 강의 관리 시스템

## 📚 문서 링크

- **[API 스펙 문서](docs/api-specification.md)** - 모든 API 엔드포인트의 상세 명세서
- **[아키텍처 문서](docs/architecture.md)** - 시스템 설계 및 기술 스택 상세 정보

## 1. 프로젝트 개요

회원가입, 강의 등록, 강의 목록 조회, 수강신청을 지원하는 RESTful API 서버입니다.

### 주요 기능

- 회원 관리 (수강생/강사 구분)
- 강의 등록 및 목록 조회 (페이징, 정렬 지원)
- 수강신청/취소 (선착순 정원 관리, 동시성 제어)
- JWT 기반 인증/인가
- 소프트 삭제 방식의 재수강 허용

### 기술 스택

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Database**: H2 (in-memory)
- **Authentication**: JWT (JSON Web Token)
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Gradle

## 2. 실행 방법

### 2.1 사전 요구사항

- JDK 17 이상
- Git

### 2.2 프로젝트 실행

1. **프로젝트 클론**

   ```bash
   git clone <repository-url>
   cd wolbu-assignment
   ```

2. **애플리케이션 실행**

   ```bash
   # Gradle Wrapper 사용 (권장)
   ./gradlew bootRun

   # 또는 JAR 빌드 후 실행
   ./gradlew build
   java -jar build/libs/assignment-0.0.1-SNAPSHOT.jar
   ```

3. **실행 확인**
   - 서버 시작: http://localhost:8080
   - 헬스 체크: `GET /api/health`

### 2.3 개발 도구 접근

- **API 문서**: http://localhost:8080/swagger-ui/index.html
- **H2 데이터베이스 콘솔**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (공백)

### 2.4 H2 콘솔 사용 (선택사항)

H2 콘솔을 사용하려면 `src/main/resources/application.properties`에서 다음 설정의 주석을 해제하세요:

```properties
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
```

## 3. API 사용 예시

### 3.1 기본 API 엔드포인트

- **회원가입**: `POST /api/auth/signup`
- **로그인**: `POST /api/auth/login`
- **강의 등록**: `POST /api/lectures` (강사만 가능)
- **강의 목록**: `GET /api/lectures?sort=RECENT&page=0&size=20`
- **수강신청**: `POST /api/enrollments` (다건 신청 가능)
- **수강취소**: `DELETE /api/enrollments/{enrollmentId}`

### 3.2 curl 예시

1. **회원가입 (수강생)**

   ```bash
   curl -X POST http://localhost:8080/api/auth/signup \
     -H "Content-Type: application/json" \
     -d '{
       "name": "홍길동",
       "email": "student@example.com",
       "phone": "010-1234-5678",
       "password": "password123",
       "role": "STUDENT"
     }'
   ```

2. **로그인**

   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "email": "student@example.com",
       "password": "password123"
     }'
   ```

3. **강의 목록 조회**

   ```bash
   curl -X GET "http://localhost:8080/api/lectures?sort=RECENT&page=0&size=20"
   ```

4. **수강신청**
   ```bash
   curl -X POST http://localhost:8080/api/enrollments \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -d '{
       "lectureIds": [1, 2, 3]
     }'
   ```

## 4. 비즈니스 규칙 요약

- 이메일 중복 불가, 비밀번호 규칙(6~10자, 영대소문자+숫자 2종 이상)
- 강사만 강의 개설 가능, 수강신청은 모두 가능
- 강의 정원 선착순 보장
- **소프트 삭제 + 재수강 허용**: 취소는 상태 변경으로 처리, 재수강 가능
- 활성 중복 신청 불가(동일 강의에 CONFIRMED 상태 1건만 허용)
- 정원 계산은 활성 등록(CONFIRMED)만 기준

## 5. 인증 규격

- 회원가입(signup): 토큰 미발급, 사용자 정보만 반환
- 로그인(login): Access Token은 응답 body(`accessToken`), Refresh Token은 HttpOnly 쿠키(`refreshToken`)
- 토큰 스키마: JWT HS256, payload에 `sub`(userId), `email`, `role`

## 6. 에러 응답 규격

- **형식**: JSON Body `{error: { "code": "COURSE_FULL", "message": "정원이 초과되었습니다." }}`
- 공통 코드 예시:

  - 400: 입력 검증 실패
  - 401/403: 인증·권한 실패
  - 404: 리소스 없음
  - 409: 정원 초과·중복 신청

## 7. 테스트

- 실행: `./gradlew test`
- 커버 범위:

  - 회원가입 검증
  - 강의 등록/목록 정렬
  - 정원 초과/중복 신청
  - 동시성 테스트 (여러 요청 → 정원 초과 없음 확인)

## 8. 테스트

### 8.1 전체 테스트 실행

```bash
./gradlew test
```

### 8.2 테스트 커버리지

- 회원가입 검증 (이메일 중복, 비밀번호 정책)
- 강의 등록/목록 정렬 기능
- 수강신청 정원 초과/중복 신청 방지
- 동시성 테스트 (다중 사용자 동시 신청 시 정원 보장)
- 재수강 허용 시나리오

### 8.3 테스트 보고서

테스트 실행 후 `build/reports/tests/test/index.html`에서 상세 결과를 확인할 수 있습니다.

## 9. 프로젝트 구조

```
src/main/java/com/company/wolbu/assignment/
├── AssignmentApplication.java          # 메인 애플리케이션 클래스
├── auth/                              # 인증/인가 도메인
│   ├── controller/                    # REST API 컨트롤러
│   ├── domain/                        # 도메인 엔티티 (Member, RefreshToken)
│   ├── dto/                           # 요청/응답 DTO
│   ├── exception/                     # 도메인 예외
│   ├── repository/                    # 데이터 접근 계층
│   ├── security/                      # JWT, 보안 설정
│   └── service/                       # 비즈니스 로직
├── common/                            # 공통 컴포넌트
│   ├── config/                        # Swagger 등 설정
│   ├── dto/                           # 공통 응답 DTO
│   ├── exception/                     # 글로벌 예외 처리
│   └── logging/                       # 로깅 설정
├── config/                            # 전역 설정
├── enrollment/                        # 수강신청 도메인
│   ├── controller/                    # 수강신청 API
│   ├── domain/                        # 수강신청 엔티티
│   ├── dto/                           # 수강신청 DTO
│   ├── exception/                     # 수강신청 예외
│   ├── repository/                    # 수강신청 레포지토리
│   └── service/                       # 수강신청 비즈니스 로직
├── health/                            # 헬스체크 API
├── lecture/                           # 강의 도메인
│   ├── controller/                    # 강의 API
│   ├── domain/                        # 강의 엔티티
│   ├── dto/                           # 강의 DTO
│   ├── exception/                     # 강의 예외
│   ├── repository/                    # 강의 레포지토리
│   └── service/                       # 강의 비즈니스 로직
```

## 10. 추가 정보

- **아키텍처 상세 문서**: [docs/architecture.md](docs/architecture.md)
- **API 문서**: 애플리케이션 실행 후 http://localhost:8080/swagger-ui/index.html
- **개발 환경**: Spring Boot 3.5.6, Java 17, H2 Database
