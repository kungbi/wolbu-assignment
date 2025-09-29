package com.company.wolbu.assignment.enrollment;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.wolbu.assignment.auth.domain.Member;
import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.repository.MemberRepository;
import com.company.wolbu.assignment.enrollment.domain.EnrollmentStatus;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentRequestDto;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentResponseDto;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentResultDto;
import com.company.wolbu.assignment.enrollment.repository.EnrollmentRepository;
import com.company.wolbu.assignment.enrollment.service.EnrollmentService;
import com.company.wolbu.assignment.lecture.domain.Lecture;
import com.company.wolbu.assignment.lecture.repository.LectureRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * 강의 신청 동시성 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EnrollmentIntegrationTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("동시성 테스트 - 정원 10명 강의에 20명이 동시 신청")
    void concurrentEnrollment_CapacityLimit() throws Exception {
        // Given
        // 강사 생성
        Member instructor = Member.create("강사", "instructor@example.com", "010-0000-0000", "password",
                MemberRole.INSTRUCTOR);
        Member savedInstructor = memberRepository.saveAndFlush(instructor);

        // 정원 10명 강의 생성
        Lecture lecture = Lecture.create("인기 강의", 10, 50000, savedInstructor.getId());
        Lecture savedLecture = lectureRepository.saveAndFlush(lecture);

        // 20명의 학생 생성 및 즉시 flush
        List<Member> students = IntStream.range(0, 20).mapToObj(
                i -> Member.create("학생" + i, "student" + i + "@example.com", "010-1234-567" + (i % 10), "password",
                        MemberRole.STUDENT)).map(student -> memberRepository.saveAndFlush(student)).toList();

        // When
        ExecutorService executor = Executors.newFixedThreadPool(20);

        try {
            List<CompletableFuture<EnrollmentResultDto>> futures = students.stream()
                    .map(student -> CompletableFuture.supplyAsync(() -> {
                        try {
                            EnrollmentRequestDto request = new EnrollmentRequestDto(List.of(savedLecture.getId()));
                            return enrollmentService.enrollInLectures(student.getId(), request);
                        } catch (Exception e) {
                            // 예외 발생 시 실패 결과 반환
                            EnrollmentResultDto.EnrollmentFailure failure = new EnrollmentResultDto.EnrollmentFailure(
                                    savedLecture.getId(), "인기 강의", "SYSTEM_ERROR", e.getMessage());
                            return new EnrollmentResultDto(List.of(), List.of(failure));
                        }
                    }, executor)).toList();

            List<EnrollmentResultDto> results = futures.stream().map(CompletableFuture::join).toList();

            // Then
            long totalSuccessCount = results.stream().mapToLong(EnrollmentResultDto::getSuccessCount).sum();

            long totalFailureCount = results.stream().mapToLong(EnrollmentResultDto::getFailureCount).sum();

            // 모든 요청이 처리되어야 함
            assertThat(totalSuccessCount + totalFailureCount).isEqualTo(20);

            // 성공한 신청은 정원(10명) 이하여야 함
            assertThat(totalSuccessCount).isLessThanOrEqualTo(10);

            // DB에서 실제 신청자 수 확인
            long actualEnrollmentCount = enrollmentRepository.countActiveByLectureId(savedLecture.getId());
            assertThat(actualEnrollmentCount).isEqualTo(totalSuccessCount);
            assertThat(actualEnrollmentCount).isLessThanOrEqualTo(10);

        } finally {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("중복 신청 방지 테스트")
    void duplicateEnrollmentPrevention() {
        // Given
        Member instructor = Member.create("강사", "instructor2@example.com", "010-0000-0001", "password",
                MemberRole.INSTRUCTOR);
        Member savedInstructor = memberRepository.save(instructor);

        Member student = Member.create("학생", "student@example.com", "010-1111-1111", "password", MemberRole.STUDENT);
        Member savedStudent = memberRepository.save(student);

        Lecture lecture = Lecture.create("테스트 강의", 10, 30000, savedInstructor.getId());
        Lecture savedLecture = lectureRepository.save(lecture);

        EnrollmentRequestDto request = new EnrollmentRequestDto(List.of(savedLecture.getId()));

        // When
        // 첫 번째 신청 (성공해야 함)
        EnrollmentResultDto firstResult = enrollmentService.enrollInLectures(savedStudent.getId(), request);

        // 두 번째 신청 (실패해야 함)
        EnrollmentResultDto secondResult = enrollmentService.enrollInLectures(savedStudent.getId(), request);

        // Then
        assertThat(firstResult.getSuccessCount()).isEqualTo(1);
        assertThat(firstResult.getFailureCount()).isEqualTo(0);

        assertThat(secondResult.getSuccessCount()).isEqualTo(0);
        assertThat(secondResult.getFailureCount()).isEqualTo(1);
        assertThat(secondResult.getFailedEnrollments().get(0).getErrorCode()).isEqualTo("ALREADY_ENROLLED_ACTIVE");

        // DB에서 실제 신청 수 확인 (1개만 있어야 함)
        long actualEnrollmentCount = enrollmentRepository.countActiveByLectureId(savedLecture.getId());
        assertThat(actualEnrollmentCount).isEqualTo(1);
    }

    @Test
    @DisplayName("재수강 허용 테스트 - 취소 후 재신청 가능")
    void reEnrollmentAfterCancellation() {
        // Given
        Member instructor = Member.create("강사", "instructor3@example.com", "010-0000-0002", "password",
                MemberRole.INSTRUCTOR);
        Member savedInstructor = memberRepository.saveAndFlush(instructor);

        Member student = Member.create("학생", "student2@example.com", "010-2222-2222", "password", MemberRole.STUDENT);
        Member savedStudent = memberRepository.saveAndFlush(student);

        Lecture lecture = Lecture.create("재수강 테스트 강의", 10, 40000, savedInstructor.getId());
        Lecture savedLecture = lectureRepository.saveAndFlush(lecture);

        EnrollmentRequestDto request = new EnrollmentRequestDto(List.of(savedLecture.getId()));

        // When
        // 1. 첫 번째 신청 (성공해야 함)
        EnrollmentResultDto firstResult = enrollmentService.enrollInLectures(savedStudent.getId(), request);
        assertThat(firstResult.getSuccessCount()).isEqualTo(1);

        // 2. 신청 취소
        Long enrollmentId = firstResult.getSuccessfulEnrollments().get(0).getEnrollmentId();
        enrollmentService.cancelEnrollment(savedStudent.getId(), enrollmentId);

        // 3. 재신청 (성공해야 함)
        EnrollmentResultDto reEnrollResult = enrollmentService.enrollInLectures(savedStudent.getId(), request);

        // Then
        assertThat(reEnrollResult.getSuccessCount()).isEqualTo(1);
        assertThat(reEnrollResult.getFailureCount()).isEqualTo(0);

        // DB에서 활성 신청 수 확인 (1개만 있어야 함)
        long activeCount = enrollmentRepository.countActiveByLectureId(savedLecture.getId());
        assertThat(activeCount).isEqualTo(1);

        // 전체 이력 확인 (취소된 것 + 활성 것 = 2개 이상)
        long totalCount =
                enrollmentRepository.countByLectureIdAndStatus(savedLecture.getId(), EnrollmentStatus.CONFIRMED)
                + enrollmentRepository.countByLectureIdAndStatus(savedLecture.getId(), EnrollmentStatus.CANCELED);
        assertThat(totalCount).isGreaterThanOrEqualTo(1); // 재활성화 또는 새 생성
    }

    @Test
    @DisplayName("통합 테스트 - 회원의 수강 신청 목록 조회")
    void getEnrollmentsByMember_IntegrationTest() {
        // Given
        Member instructor = Member.create("강사", "instructor4@example.com", "010-0000-0003", "password",
                MemberRole.INSTRUCTOR);
        Member savedInstructor = memberRepository.saveAndFlush(instructor);

        Member student = Member.create("학생", "student3@example.com", "010-3333-3333", "password", MemberRole.STUDENT);
        Member savedStudent = memberRepository.saveAndFlush(student);

        // 여러 강의 생성
        Lecture lecture1 = Lecture.create("자바 프로그래밍", 15, 100000, savedInstructor.getId());
        Lecture lecture2 = Lecture.create("스프링 부트", 20, 120000, savedInstructor.getId());
        Lecture lecture3 = Lecture.create("데이터베이스", 25, 80000, savedInstructor.getId());

        Lecture savedLecture1 = lectureRepository.saveAndFlush(lecture1);
        Lecture savedLecture2 = lectureRepository.saveAndFlush(lecture2);
        Lecture savedLecture3 = lectureRepository.saveAndFlush(lecture3);
        System.out.println("Saved Lecture IDs: " + savedLecture1.getId() + ", " + savedLecture2.getId() + ", "
                           + savedLecture3.getId());

        // 강의 신청
        EnrollmentRequestDto request1 = new EnrollmentRequestDto(List.of(savedLecture1.getId()));
        EnrollmentRequestDto request2 = new EnrollmentRequestDto(List.of(savedLecture2.getId()));
        EnrollmentRequestDto request3 = new EnrollmentRequestDto(List.of(savedLecture3.getId()));

        enrollmentService.enrollInLectures(savedStudent.getId(), request1);
        enrollmentService.enrollInLectures(savedStudent.getId(), request2);
        EnrollmentResultDto result3 = enrollmentService.enrollInLectures(savedStudent.getId(), request3);

        // 세 번째 강의는 취소
        Long enrollmentId3 = result3.getSuccessfulEnrollments().get(0).getEnrollmentId();
        enrollmentService.cancelEnrollment(savedStudent.getId(), enrollmentId3);

        // When
        List<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByMember(savedStudent.getId());

        // Then
        // 활성 상태인 강의만 조회되어야 함 (첫 번째, 두 번째 강의만)
        assertThat(enrollments).hasSize(2);

        List<String> lectureTitles = enrollments.stream().map(EnrollmentResponseDto::getLectureTitle).toList();

        assertThat(lectureTitles).containsExactlyInAnyOrder("자바 프로그래밍", "스프링 부트");
        assertThat(lectureTitles).doesNotContain("데이터베이스"); // 취소된 강의는 포함되지 않음

        // 모든 수강 신청의 상태가 CONFIRMED인지 확인
        enrollments.forEach(enrollment -> {
            assertThat(enrollment.getStatus()).isEqualTo("CONFIRMED");
            assertThat(enrollment.getMemberId()).isEqualTo(savedStudent.getId());
        });
    }
}
