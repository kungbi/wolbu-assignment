# 📑 README.md (실행/사용자 가이드 중심)

## 1. 프로젝트 개요

- 목적: 회원가입, 강의 등록, 강의 목록 조회, 수강신청 API 제공
- 특징:

  - 회원 유형(수강생/강사)
  - 강의 목록 페이지네이션(20개/페이지, 정렬 3종)
  - 수강신청 선착순 정원 보장 및 동시성 제어

## 2. 실행 방법

- **필수 환경**: JDK 17+, Gradle, (기본 DB: H2 in-memory)
- **명령어**:

  ```bash
  ./gradlew bootRun
  ```

- **API 문서**: Swagger UI → `/swagger-ui/index.html`
- **DB 콘솔**: H2 → `/h2-console` (`jdbc:h2:mem:testdb`)

## 3. API 사용 예시

- 회원가입: `POST /api/members`
- 강의등록: `POST /api/courses` (강사만 가능)
- 강의목록: `GET /api/courses?sort=RECENT&page=1&size=20`
- 수강신청: `POST /api/enrollments` (다건 신청 가능)

간단한 **curl 예시** 포함.

## 4. 비즈니스 규칙 요약

- 이메일 중복 불가, 비밀번호 규칙(6~10자, 영대소문자+숫자 2종 이상)
- 강사만 강의 개설 가능, 수강신청은 모두 가능
- 강의 정원 선착순 보장
- 중복 신청 불가(회원-강의 unique)

## 5. 에러 응답 규격

- **형식**: JSON `{ "status": 409, "code": "COURSE_FULL", "message": "정원이 초과되었습니다." }`
- 공통 코드 예시:

  - 400: 입력 검증 실패
  - 401/403: 인증·권한 실패
  - 404: 리소스 없음
  - 409: 정원 초과·중복 신청

## 6. 테스트

- 실행: `./gradlew test`
- 커버 범위:

  - 회원가입 검증
  - 강의 등록/목록 정렬
  - 정원 초과/중복 신청
  - 동시성 테스트 (여러 요청 → 정원 초과 없음 확인)

## 7. 폴더 구조

```
src/main/java/com/example/woolbu
  ├─ web (controller, dto)
  ├─ application (service)
  ├─ domain (entity, policy)
  └─ infra (repository)
```
