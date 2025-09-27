# 🧠 docs/architecture.md (설계/근거 중심)

## 1. 아키텍처 개요

- Spring Boot + JPA 기반
- 레이어 구조:

  - Web (Controller/DTO)
  - Application (Service, 트랜잭션 처리)
  - Domain (Entity, 비즈니스 규칙)
  - Infra (Repository, DB 접근)

## 2. 도메인 모델

- **Member**: id, name, email, phone, passwordHash, role
- **Lecture**: id, title, maxCapacity, price, instructorId, createdAt
- **Enrollment**: id, lectureId, memberId, status(CONFIRMED/CANCELED), createdAt, updatedAt, canceledAt
- **관계**:

  - 강사(Member) → 강의(Lecture) (1:N)
  - 회원(Member) → 수강신청(Enrollment) → 강의(Lecture) (N:M)
  - 소프트 삭제 정책: 취소 시 물리 삭제 없이 상태만 CANCELED로 변경

## 3. 데이터베이스 설계

- **테이블 제약**:

  - Member.email unique
  - Enrollment(course_id, member_id) unique
  - Course.max_capacity ≥ 1

- **인덱스**:

  - Course(created_at) → 최근 등록순
  - Enrollment(course_id) → 신청자 수 집계

## 4. 페이징·정렬

- 페이지 기반 (size=20 고정)
- 정렬 옵션:

  - RECENT (등록일 DESC)
  - POPULAR_COUNT (신청자 수 DESC)
  - POPULAR_RATE (신청률 DESC = 신청자 ÷ 정원)

## 5. 수강신청 동시성 제어

- DB 트랜잭션 단위에서 정원 확인 및 insert 처리
- 중복 신청은 DB unique 제약으로 방지
- 정원 초과 시 에러 반환

## 6. 예외 처리

- 전역 예외 핸들러 사용
- 모든 에러 JSON 표준화

## 7. 확장 고려

- 강의 설명, 태그 등 변경이 잦은 필드는 JSON 컬럼으로 보완 가능
- 대규모 트래픽 시 커서 기반 페이징, 캐시, 분산락 고려 가능
- 향후 기능: 수강 취소, 강의 상태(마감 여부), JWT 인증 추가

## 8. 수강신청 취소/재수강 요구사항 (소프트 삭제 + 재수강 허용)

### 8.1 목표/범위

- 사용자는 수강 취소 후 동일 강의를 재수강(재등록) 할 수 있어야 한다.
- 취소는 **소프트 삭제(상태 전환)**로 처리한다. 물리 삭제는 하지 않는다.
- 정원(최대 수강 인원) 규칙은 항상 활성 등록만 기준으로 적용한다.

### 8.2 데이터 모델

**enrollment 테이블(핵심 컬럼)**

- id (PK)
- lecture_id (FK)
- member_id (FK)
- status ENUM('CONFIRMED','CANCELED') // 기본값 CONFIRMED
- created_at, updated_at
- canceled_at // 취소 시각 기록

**유일성 제약(활성만 유일)**

- H2: 인덱스 기반 제약 (idx_lecture_member_status)
- 활성 등록(CONFIRMED)에서만 중복 방지

### 8.3 비즈니스 규칙

- **재수강 허용**: status='CANCELED'인 과거 이력이 있어도 새로운 CONFIRMED 등록 가능.
- **정원 체크**: 현재 활성 등록 수(status='CONFIRMED') < lecture.max_capacity일 때만 신규 등록 허용.
- **중복 활성 금지**: 한 회원은 동일 강좌에 동시에 활성 등록 1건만 보유 가능.
- **취소 처리**: 활성 등록을 status='CANCELED'로 변경(소프트 삭제).
- **집계/노출 기준**: "현재 신청자 수", "신청률", "정렬"은 활성 등록만 집계한다.

### 8.4 API 요구사항

**수강 신청(다건)**

- POST /api/enrollments
- 처리: 각 lectureId에 대해 트랜잭션 내에서 Lecture 행 잠금(비관적 락) → 활성 등록 수 조회 → 정원 체크 → 활성 중복 확인 → 새 Enrollment(status='CONFIRMED') INSERT
- 데드락 방지: 항상 같은 순서(오름차순)로 처리
- 응답: 부분 성공 허용, 성공/실패 항목 배열 제공

**수강 취소(단건)**

- DELETE /api/enrollments/{id}
- 처리: Lecture 행 잠금 → 대상 Enrollment가 활성인지 확인 → status='CANCELED', canceled_at=NOW() 업데이트
- 응답: 성공 200 OK, 이미 취소 상태 409 CONFLICT

### 8.5 트랜잭션/동시성

- **락 대상**: 해당 Lecture의 PK 1행
- **락 방식**: 비관적 쓰기 락(JPA PESSIMISTIC_WRITE)
- **흐름(등록)**: Lecture 잠금 → 활성 수 카운트 → 정원 체크 → 활성 중복 확인 → INSERT → 커밋
- **흐름(취소)**: Lecture 잠금 → 활성 여부 확인 → 상태 전환 → 커밋

### 8.6 에러/검증 규격

- 409 CONFLICT: COURSE_FULL (정원 초과), ALREADY_ENROLLED_ACTIVE (활성 중복), ALREADY_CANCELED (이미 취소 상태)
- 404 NOT_FOUND: LECTURE_NOT_FOUND, ENROLLMENT_NOT_FOUND
- 400 BAD_REQUEST: VALIDATION_FAILED

## 9. 테스트 전략

- 단위 테스트: 비밀번호 검증, 신청률 계산, 소프트 삭제 로직
- 리포지토리 테스트: unique 제약, 인덱스 동작 확인
- 통합 테스트: 주요 API happy/에러 플로우, 재수강 허용 시나리오
- 동시성 테스트: 다중 스레드로 동일 강의 신청 시 정원 보장 확인, 취소와 등록 경합 상황
