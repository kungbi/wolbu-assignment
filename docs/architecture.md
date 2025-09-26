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
- **Course**: id, title, maxCapacity, price, instructorId, createdAt
- **Enrollment**: id, courseId, memberId, status, createdAt
- **관계**:

  - 강사(Member) → 강의(Course) (1:N)
  - 회원(Member) → 수강신청(Enrollment) → 강의(Course) (N:M)

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

## 8. 테스트 전략

- 단위 테스트: 비밀번호 검증, 신청률 계산
- 리포지토리 테스트: unique 제약, 인덱스 동작 확인
- 통합 테스트: 주요 API happy/에러 플로우
- 동시성 테스트: 다중 스레드로 동일 강의 신청 시 정원 보장 확인
