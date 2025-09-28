package com.company.wolbu.assignment.enrollment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.company.wolbu.assignment.auth.domain.Member;
import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.repository.MemberRepository;
import com.company.wolbu.assignment.enrollment.domain.Enrollment;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentRequest;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentResult;
import com.company.wolbu.assignment.enrollment.exception.MemberNotFoundException;
import com.company.wolbu.assignment.enrollment.exception.EnrollmentNotFoundException;
import com.company.wolbu.assignment.enrollment.exception.UnauthorizedEnrollmentException;
import com.company.wolbu.assignment.enrollment.exception.AlreadyCanceledException;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentResponse;
import com.company.wolbu.assignment.enrollment.repository.EnrollmentRepository;
import com.company.wolbu.assignment.enrollment.service.EnrollmentService;
import com.company.wolbu.assignment.lecture.domain.Lecture;
import com.company.wolbu.assignment.lecture.repository.LectureRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * EnrollmentService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Member testMember;
    private Lecture testLecture;

    @BeforeEach
    void setUp() {
        testMember = Member.create("홍길동", "test@example.com", "010-1234-5678", "hashedPassword", MemberRole.STUDENT);
        testLecture = Lecture.create("테스트 강의", 10, 50000, 1L);
    }

    @Test
    @DisplayName("강의 신청 성공")
    void enrollInLectures_Success() {
        // Given
        Long memberId = 1L;
        Long lectureId = 1L;
        EnrollmentRequest request = new EnrollmentRequest(List.of(lectureId));

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(lectureRepository.findByIdWithLock(lectureId)).thenReturn(Optional.of(testLecture));
        when(enrollmentRepository.existsActiveByLectureIdAndMemberId(lectureId, memberId)).thenReturn(false);
        when(enrollmentRepository.countActiveByLectureId(lectureId)).thenReturn(5L);
        when(enrollmentRepository.findByLectureIdAndMemberId(lectureId, memberId)).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment enrollment = invocation.getArgument(0);
            // ID 설정 (실제 저장 시뮬레이션)
            return Enrollment.create(enrollment.getLectureId(), enrollment.getMemberId());
        });

        // When
        EnrollmentResult result = enrollmentService.enrollInLectures(memberId, request);

        // Then
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(result.getSuccessfulEnrollments()).hasSize(1);
        assertThat(result.getSuccessfulEnrollments().get(0).getLectureId()).isEqualTo(lectureId);
    }

    @Test
    @DisplayName("정원 초과로 인한 강의 신청 실패")
    void enrollInLectures_CourseFull() {
        // Given
        Long memberId = 1L;
        Long lectureId = 1L;
        EnrollmentRequest request = new EnrollmentRequest(List.of(lectureId));

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(lectureRepository.findByIdWithLock(lectureId)).thenReturn(Optional.of(testLecture));
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));
        when(enrollmentRepository.existsActiveByLectureIdAndMemberId(lectureId, memberId)).thenReturn(false);
        // 정원(10명)과 같은 수의 신청자
        when(enrollmentRepository.countActiveByLectureId(lectureId)).thenReturn(10L);

        // When
        EnrollmentResult result = enrollmentService.enrollInLectures(memberId, request);

        // Then
        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailedEnrollments()).hasSize(1);
        assertThat(result.getFailedEnrollments().get(0).getErrorCode()).isEqualTo("COURSE_FULL");
    }

    @Test
    @DisplayName("중복 신청으로 인한 강의 신청 실패")
    void enrollInLectures_DuplicateEnrollment() {
        // Given
        Long memberId = 1L;
        Long lectureId = 1L;
        EnrollmentRequest request = new EnrollmentRequest(List.of(lectureId));

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        // 이미 신청한 상태
        when(lectureRepository.findByIdWithLock(lectureId)).thenReturn(Optional.of(testLecture));
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));
        when(enrollmentRepository.existsActiveByLectureIdAndMemberId(lectureId, memberId)).thenReturn(true);

        // When
        EnrollmentResult result = enrollmentService.enrollInLectures(memberId, request);

        // Then
        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailedEnrollments()).hasSize(1);
        assertThat(result.getFailedEnrollments().get(0).getErrorCode()).isEqualTo("ALREADY_ENROLLED_ACTIVE");
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 강의 신청 시 예외 발생")
    void enrollInLectures_MemberNotFound() {
        // Given
        Long memberId = 999L;
        EnrollmentRequest request = new EnrollmentRequest(List.of(1L));

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> enrollmentService.enrollInLectures(memberId, request))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("회원을 찾을 수 없습니다. (ID: 999)");
    }

    @Test
    @DisplayName("여러 강의 동시 신청 - 일부 성공, 일부 실패")
    void enrollInLectures_PartialSuccess() {
        // Given
        Long memberId = 1L;
        Long lectureId1 = 1L; // 성공할 강의
        Long lectureId2 = 2L; // 정원 초과로 실패할 강의
        EnrollmentRequest request = new EnrollmentRequest(List.of(lectureId1, lectureId2));

        Lecture fullLecture = Lecture.create("정원 초과 강의", 5, 30000, 1L);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(lectureRepository.findByIdWithLock(lectureId1)).thenReturn(Optional.of(testLecture));
        when(lectureRepository.findByIdWithLock(lectureId2)).thenReturn(Optional.of(fullLecture));
        when(lectureRepository.findById(lectureId2)).thenReturn(Optional.of(fullLecture));

        // 첫 번째 강의: 성공 조건
        when(enrollmentRepository.existsActiveByLectureIdAndMemberId(lectureId1, memberId)).thenReturn(false);
        when(enrollmentRepository.countActiveByLectureId(lectureId1)).thenReturn(3L);
        when(enrollmentRepository.findByLectureIdAndMemberId(lectureId1, memberId)).thenReturn(Optional.empty());

        // 두 번째 강의: 정원 초과
        when(enrollmentRepository.existsActiveByLectureIdAndMemberId(lectureId2, memberId)).thenReturn(false);
        when(enrollmentRepository.countActiveByLectureId(lectureId2)).thenReturn(5L);

        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment enrollment = invocation.getArgument(0);
            return Enrollment.create(enrollment.getLectureId(), enrollment.getMemberId());
        });

        // When
        EnrollmentResult result = enrollmentService.enrollInLectures(memberId, request);

        // Then
        assertThat(result.getTotalRequested()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getSuccessfulEnrollments().get(0).getLectureId()).isEqualTo(lectureId1);
        assertThat(result.getFailedEnrollments().get(0).getLectureId()).isEqualTo(lectureId2);
        assertThat(result.getFailedEnrollments().get(0).getErrorCode()).isEqualTo("COURSE_FULL");
    }

    @Test
    @DisplayName("재수강 허용 - 취소된 강의를 다시 신청할 수 있다")
    void enrollInLectures_ReEnrollmentAllowed() {
        // Given
        Long memberId = 1L;
        Long lectureId = 1L;
        EnrollmentRequest request = new EnrollmentRequest(List.of(lectureId));

        // 기존 취소된 수강 신청 생성
        Enrollment canceledEnrollment = Enrollment.create(lectureId, memberId);
        canceledEnrollment.cancel();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(lectureRepository.findByIdWithLock(lectureId)).thenReturn(Optional.of(testLecture));
        when(enrollmentRepository.existsActiveByLectureIdAndMemberId(lectureId, memberId)).thenReturn(false);
        when(enrollmentRepository.countActiveByLectureId(lectureId)).thenReturn(5L);
        when(enrollmentRepository.findByLectureIdAndMemberId(lectureId, memberId))
                .thenReturn(Optional.of(canceledEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EnrollmentResult result = enrollmentService.enrollInLectures(memberId, request);

        // Then
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(result.getSuccessfulEnrollments()).hasSize(1);
        assertThat(result.getSuccessfulEnrollments().get(0).getLectureId()).isEqualTo(lectureId);

        // 기존 취소 신청이 재활성화되었는지 확인
        assertThat(canceledEnrollment.isActive()).isTrue();
        assertThat(canceledEnrollment.isCanceled()).isFalse();
    }

    @Test
    @DisplayName("수강 신청 취소 성공")
    void cancelEnrollment_Success() {
        // Given
        Long memberId = 1L;
        Long enrollmentId = 100L;
        Long lectureId = 1L;
        
        Enrollment activeEnrollment = Enrollment.create(lectureId, memberId);
        
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(activeEnrollment));
        when(lectureRepository.findByIdWithLock(lectureId)).thenReturn(Optional.of(testLecture));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        enrollmentService.cancelEnrollment(memberId, enrollmentId);

        // Then
        assertThat(activeEnrollment.isCanceled()).isTrue();
        assertThat(activeEnrollment.isActive()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 수강 신청 취소 시 예외 발생")
    void cancelEnrollment_EnrollmentNotFound() {
        // Given
        Long memberId = 1L;
        Long enrollmentId = 999L;

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(memberId, enrollmentId))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessage("수강 신청을 찾을 수 없습니다. (ID: 999)");
    }

    @Test
    @DisplayName("다른 회원의 수강 신청 취소 시 예외 발생")
    void cancelEnrollment_UnauthorizedAccess() {
        // Given
        Long requestMemberId = 1L;
        Long actualMemberId = 2L;
        Long enrollmentId = 100L;
        Long lectureId = 1L;
        
        Enrollment otherMemberEnrollment = Enrollment.create(lectureId, actualMemberId);

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(otherMemberEnrollment));

        // When & Then
        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(requestMemberId, enrollmentId))
                .isInstanceOf(UnauthorizedEnrollmentException.class)
                .hasMessage("본인의 수강 신청만 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("이미 취소된 수강 신청 재취소 시 예외 발생")
    void cancelEnrollment_AlreadyCanceled() {
        // Given
        Long memberId = 1L;
        Long enrollmentId = 100L;
        Long lectureId = 1L;
        
        Enrollment canceledEnrollment = Enrollment.create(lectureId, memberId);
        canceledEnrollment.cancel(); // 미리 취소 상태로 변경

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(canceledEnrollment));

        // When & Then
        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(memberId, enrollmentId))
                .isInstanceOf(AlreadyCanceledException.class)
                .hasMessage("이미 취소된 수강 신청입니다. (수강 신청 ID: 100)");
    }

    @Test
    @DisplayName("회원의 수강 신청 목록 조회 성공")
    void getEnrollmentsByMember_Success() {
        // Given
        Long memberId = 1L;
        Long lectureId1 = 1L;
        Long lectureId2 = 2L;
        
        Enrollment enrollment1 = Enrollment.create(lectureId1, memberId);
        Enrollment enrollment2 = Enrollment.create(lectureId2, memberId);
        
        Lecture lecture2 = Lecture.create("두 번째 강의", 20, 60000, 1L);

        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(enrollmentRepository.findByMemberIdAndStatus(memberId, com.company.wolbu.assignment.enrollment.domain.EnrollmentStatus.CONFIRMED))
                .thenReturn(List.of(enrollment1, enrollment2));
        when(lectureRepository.findById(lectureId1)).thenReturn(Optional.of(testLecture));
        when(lectureRepository.findById(lectureId2)).thenReturn(Optional.of(lecture2));

        // When
        List<EnrollmentResponse> result = enrollmentService.getEnrollmentsByMember(memberId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLectureId()).isEqualTo(lectureId1);
        assertThat(result.get(0).getLectureTitle()).isEqualTo("테스트 강의");
        assertThat(result.get(0).getMemberId()).isEqualTo(memberId);
        assertThat(result.get(0).getStatus()).isEqualTo("CONFIRMED");
        
        assertThat(result.get(1).getLectureId()).isEqualTo(lectureId2);
        assertThat(result.get(1).getLectureTitle()).isEqualTo("두 번째 강의");
        assertThat(result.get(1).getMemberId()).isEqualTo(memberId);
        assertThat(result.get(1).getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("존재하지 않는 회원의 수강 신청 목록 조회 시 예외 발생")
    void getEnrollmentsByMember_MemberNotFound() {
        // Given
        Long memberId = 999L;

        when(memberRepository.existsById(memberId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> enrollmentService.getEnrollmentsByMember(memberId))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("회원을 찾을 수 없습니다. (ID: 999)");
    }

    @Test
    @DisplayName("수강 신청이 없는 회원의 목록 조회 시 빈 리스트 반환")
    void getEnrollmentsByMember_EmptyList() {
        // Given
        Long memberId = 1L;

        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(enrollmentRepository.findByMemberIdAndStatus(memberId, com.company.wolbu.assignment.enrollment.domain.EnrollmentStatus.CONFIRMED))
                .thenReturn(List.of());

        // When
        List<EnrollmentResponse> result = enrollmentService.getEnrollmentsByMember(memberId);

        // Then
        assertThat(result).isEmpty();
    }
}
