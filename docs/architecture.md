# 🏗️ 월부 강의 관리 시스템 아키텍처

## 1. 시스템 아키텍처 개요

### 1.1 기술 스택

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA + Hibernate
- **Security**: Spring Security + JWT
- **Documentation**: SpringDoc OpenAPI 3
- **Build**: Gradle 8.x
- **Test**: JUnit 5, MockMvc

### 1.2 아키텍처 패턴

도메인 주도 설계(DDD) 원칙을 적용한 계층형 아키텍처를 채택했습니다.

```
┌─────────────────────┐
│   Presentation      │ ← Controller, DTO, Exception Handler
├─────────────────────┤
│   Application       │ ← Service, Transaction Management
├─────────────────────┤
│   Domain            │ ← Entity, Domain Logic, Policy
├─────────────────────┤
│   Infrastructure    │ ← Repository, External Integration
└─────────────────────┘
```

### 1.3 도메인 분리

비즈니스 도메인별로 패키지를 분리하여 응집도를 높이고 결합도를 낮췄습니다:

- **auth**: 회원 인증/인가
- **lecture**: 강의 관리
- **enrollment**: 수강신청 관리
- **common**: 공통 기능 (예외, 응답, 설정)

## 2. 도메인 모델 설계

### 2.1 핵심 엔티티

#### Member (회원)

```java
@Entity
public class Member {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;           // 로그인 ID (중복 불가)

    private String name;            // 회원명
    private String phone;           // 연락처
    private String passwordHash;    // 암호화된 비밀번호

    @Enumerated(EnumType.STRING)
    private MemberRole role;        // STUDENT, INSTRUCTOR

    private LocalDateTime createdAt;
}
```

#### Lecture (강의)

```java
@Entity
public class Lecture {
    @Id @GeneratedValue
    private Long id;

    private String title;           // 강의명
    private Integer maxCapacity;    // 최대 수강 인원
    private BigDecimal price;       // 수강료

    @ManyToOne(fetch = FetchType.LAZY)
    private Member instructor;      // 강사 (외래키)

    private LocalDateTime createdAt;
}
```

#### Enrollment (수강신청)

```java
@Entity
public class Enrollment {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;        // 강의 (외래키)

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;          // 회원 (외래키)

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status; // CONFIRMED, CANCELED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime canceledAt; // 취소 시각
}
```

### 2.2 도메인 관계

- **강사(Member) → 강의(Lecture)**: 1:N 관계
- **회원(Member) → 수강신청(Enrollment)**: 1:N 관계
- **강의(Lecture) → 수강신청(Enrollment)**: 1:N 관계
- **소프트 삭제**: 취소 시 물리 삭제 없이 상태만 CANCELED로 변경

## 3. 데이터베이스 설계

### 3.1 테이블 제약사항

- **member.email**: UNIQUE 제약 (중복 가입 방지)
- **enrollment(lecture_id, member_id, status)**: 복합 인덱스 (활성 등록 중복 방지)
- **lecture.max_capacity**: 1 이상의 양수 값

### 3.2 인덱스 전략

- **lecture(created_at)**: 최신 등록순 정렬을 위한 인덱스
- **enrollment(lecture_id)**: 수강신청자 수 집계 성능 향상
- **enrollment(member_id, status)**: 회원별 활성 수강신청 조회 최적화

### 3.3 데이터 무결성

- **외래키 제약**: JPA @ManyToOne으로 참조 무결성 보장
- **소프트 삭제**: 물리 삭제 대신 status 필드로 논리 삭제 처리
- **동시성 제어**: 비관적 락(PESSIMISTIC_WRITE)으로 정원 초과 방지

## 4. API 설계 원칙

### 4.1 RESTful API 설계

- **리소스 중심**: `/api/lectures`, `/api/enrollments`
- **HTTP 메서드**: GET(조회), POST(생성), DELETE(삭제)
- **상태 코드**: 200(성공), 201(생성), 400(잘못된 요청), 404(없음), 409(충돌)

### 4.2 페이징 및 정렬

#### 페이징 파라미터

- **page**: 페이지 번호 (0부터 시작)
- **size**: 페이지 크기 (기본값: 20, 최대: 100)

#### 정렬 옵션

- **RECENT**: 등록일 기준 내림차순 (최신순)
- **POPULAR_COUNT**: 수강신청자 수 기준 내림차순
- **POPULAR_RATE**: 수강신청률 기준 내림차순 (신청자 수 ÷ 정원)

### 4.3 응답 형식 표준화

```json
{
	"success": true,
	"data": {
		/* 실제 데이터 */
	},
	"error": null
}
```

에러 시:

```json
{
	"status": 409,
	"code": "COURSE_FULL",
	"message": "정원이 초과되었습니다."
}
```

## 5. 보안 및 인증

### 5.1 JWT 기반 인증

- **Access Token**: 응답 body에 포함 (만료: 1시간)
- **Refresh Token**: HttpOnly 쿠키로 전송 (만료: 2주)
- **토큰 구조**: Header.Payload.Signature (HS256 알고리즘)

### 5.2 비밀번호 정책

- **길이**: 6~10자
- **복잡도**: 영대소문자, 숫자 중 2종 이상 포함
- **암호화**: BCrypt 해시 함수 사용

### 5.3 권한 관리

- **STUDENT**: 수강신청/취소만 가능
- **INSTRUCTOR**: 강의 등록 + 수강신청/취소 가능
- **@RequireRole** 어노테이션으로 메서드 레벨 권한 제어

## 6. 동시성 제어 및 트랜잭션

### 6.1 수강신청 동시성 처리

```java
@Transactional
public EnrollmentResult enrollInLecture(Long lectureId, Long memberId) {
    // 1. 강의 정보를 비관적 락으로 조회
    Lecture lecture = lectureRepository.findByIdWithLock(lectureId);

    // 2. 현재 활성 수강신청 수 확인
    long currentCount = enrollmentRepository.countByLectureIdAndStatus(
        lectureId, EnrollmentStatus.CONFIRMED);

    // 3. 정원 초과 검사
    if (currentCount >= lecture.getMaxCapacity()) {
        throw new CourseFullException();
    }

    // 4. 중복 신청 검사
    if (enrollmentRepository.existsActiveBetween(lectureId, memberId)) {
        throw new AlreadyEnrolledException();
    }

    // 5. 수강신청 등록
    Enrollment enrollment = new Enrollment(lecture, member);
    return enrollmentRepository.save(enrollment);
}
```

### 6.2 트랜잭션 관리

- **@Transactional**: Service 계층에서 선언적 트랜잭션 관리
- **비관적 락**: 정원 초과 방지를 위한 행 레벨 락
- **격리 수준**: READ_COMMITTED (기본값)

## 7. 예외 처리 전략

### 7.1 예외 계층 구조

```
BusinessException (추상)
├── AuthException
│   ├── DuplicateEmailException
│   ├── InvalidCredentialsException
│   └── InvalidPasswordPolicyException
├── LectureException
│   ├── LectureNotFoundException
│   └── InstructorOnlyException
└── EnrollmentException
    ├── CourseFullException
    ├── AlreadyEnrolledException
    └── EnrollmentNotFoundException
```

### 7.2 글로벌 예외 처리

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity
            .status(e.getHttpStatus())
            .body(new ErrorResponse(e.getErrorCode(), e.getMessage()));
    }
}
```

## 8. 소프트 삭제 및 재수강 정책

### 8.1 설계 원칙

- **재수강 허용**: 취소된 강의를 다시 수강신청할 수 있음
- **소프트 삭제**: 물리 삭제 없이 상태 변경으로 처리
- **정원 관리**: 활성 상태(CONFIRMED)만 정원에 포함

### 8.2 구현 방식

#### 상태 관리

```java
public enum EnrollmentStatus {
    CONFIRMED,  // 활성 수강신청
    CANCELED    // 취소된 수강신청
}
```

#### 비즈니스 규칙

1. **중복 활성 방지**: 동일 강의에 대해 CONFIRMED 상태 1건만 허용
2. **정원 계산**: CONFIRMED 상태만 집계하여 정원 관리
3. **재수강 허용**: CANCELED 이력이 있어도 새로운 CONFIRMED 등록 가능
4. **취소 처리**: DELETE 요청 시 status를 CANCELED로 변경

### 8.3 동시성 제어

```java
// 수강신청 시
@Lock(LockModeType.PESSIMISTIC_WRITE)
Lecture findByIdWithLock(Long id);

// 정원 확인
long countConfirmedEnrollments = enrollmentRepository
    .countByLectureIdAndStatus(lectureId, CONFIRMED);
```

## 9. 테스트 전략

### 9.1 테스트 현황

#### 도메인별 테스트 개수

- **Auth 도메인**: 47개 테스트 (인증/인가, JWT, 회원 관리)
- **Enrollment 도메인**: 23개 테스트 (수강신청, 취소, 동시성 제어)
- **Lecture 도메인**: 20개 테스트 (강의 관리, 목록 조회)
- **전체**: 총 91개 테스트

### 9.2 테스트 계층

- **단위 테스트**: 도메인 로직, 비즈니스 규칙 검증
- **통합 테스트**: API 엔드포인트, 데이터베이스 연동 테스트
- **동시성 테스트**: 멀티스레드 환경에서의 정원 관리 검증

### 9.3 주요 테스트 케이스

#### 인증/인가

- 비밀번호 정책 검증 (길이, 복잡도)
- JWT 토큰 생성/검증
- 권한별 접근 제어

#### 강의 관리

- 강의 등록 (강사 권한 검증)
- 강의 목록 조회 (페이징, 정렬)
- 신청률 계산 로직

#### 수강신청

- 정원 초과 방지
- 중복 신청 방지
- 소프트 삭제 및 재수강 허용
- 동시 신청 시 정원 보장

### 9.4 테스트 도구

- **JUnit 5**: 테스트 프레임워크
- **MockMvc**: 웹 계층 테스트
- **@DataJpaTest**: 레포지토리 계층 테스트
- **@SpringBootTest**: 통합 테스트
- **ExecutorService**: 동시성 테스트

## 10. 확장 고려사항

### 10.1 성능 최적화

- **캐싱**: Redis를 활용한 강의 목록 캐싱
- **인덱스**: 자주 조회되는 컬럼에 대한 인덱스 추가
- **커서 페이징**: 대용량 데이터 처리 시 오프셋 페이징 대신 커서 방식 고려

### 10.2 확장성

- **이벤트 기반 아키텍처**: 수강신청 완료 시 알림 발송 등
- **마이크로서비스**: 도메인별 서비스 분리
- **분산 락**: Redis 기반 분산 락으로 동시성 제어 개선

### 10.3 운영 고려사항

- **모니터링**: Actuator, Prometheus, Grafana 연동
- **로깅**: 구조화된 로깅, 분산 추적
- **배포**: Docker 컨테이너화, K8s 배포
- **백업**: 데이터베이스 정기 백업 정책
