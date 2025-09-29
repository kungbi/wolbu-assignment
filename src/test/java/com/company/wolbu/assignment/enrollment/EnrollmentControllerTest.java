package com.company.wolbu.assignment.enrollment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.company.wolbu.assignment.auth.domain.Member;
import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.repository.MemberRepository;
import com.company.wolbu.assignment.auth.security.JwtProvider;
import com.company.wolbu.assignment.enrollment.domain.Enrollment;
import com.company.wolbu.assignment.enrollment.domain.EnrollmentStatus;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentRequest;
import com.company.wolbu.assignment.enrollment.repository.EnrollmentRepository;
import com.company.wolbu.assignment.lecture.domain.Lecture;
import com.company.wolbu.assignment.lecture.repository.LectureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Long instructorId;
    private Long studentId;
    private Long otherStudentId;
    private Long lectureIdA;
    private Long lectureIdB;
    private Long lectureIdLimited;
    private String studentToken;

    @BeforeEach
    void setUp() {
        Member instructor = memberRepository.save(Member.create(
                "강사", "instructor@example.com", "010-0000-0000", "hashedPassword", MemberRole.INSTRUCTOR));
        instructorId = instructor.getId();

        Member student = memberRepository.save(Member.create(
                "수강생", "student@example.com", "010-1111-1111", "hashedPassword", MemberRole.STUDENT));
        studentId = student.getId();

        Member otherStudent = memberRepository.save(Member.create(
                "다른수강생", "other@student.com", "010-2222-2222", "hashedPassword", MemberRole.STUDENT));
        otherStudentId = otherStudent.getId();

        Lecture lectureA = lectureRepository.save(Lecture.create("내집마련 기초", 5, 200000, instructorId));
        Lecture lectureB = lectureRepository.save(Lecture.create("부동산 실전", 5, 250000, instructorId));
        Lecture lectureLimited = lectureRepository.save(Lecture.create("정원 제한 강의", 1, 100000, instructorId));

        lectureIdA = lectureA.getId();
        lectureIdB = lectureB.getId();
        lectureIdLimited = lectureLimited.getId();

        studentToken = jwtProvider.generateAccessToken(studentId, student.getEmail(), student.getRole());
    }

    @Test
    @DisplayName("수강생이 여러 강의를 성공적으로 신청한다")
    void enrollLectures_success() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest(List.of(lectureIdA, lectureIdB));
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/enrollments")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.failedEnrollments").isEmpty())
                .andExpect(jsonPath("$.data.successfulEnrollments[0].enrollmentId").exists())
                .andExpect(jsonPath("$.data.successfulEnrollments[0].lectureId").value(lectureIdA))
                .andExpect(jsonPath("$.data.successfulEnrollments[1].lectureId").value(lectureIdB));

        assertThat(enrollmentRepository.existsActiveByLectureIdAndMemberId(lectureIdA, studentId)).isTrue();
        assertThat(enrollmentRepository.existsActiveByLectureIdAndMemberId(lectureIdB, studentId)).isTrue();
    }

    @Test
    @DisplayName("정원이 가득 찬 강의 신청 시 409를 반환한다")
    void enrollLecture_courseFull_conflict() throws Exception {
        enrollmentRepository.save(Enrollment.create(lectureIdLimited, otherStudentId));

        EnrollmentRequest request = new EnrollmentRequest(List.of(lectureIdLimited));
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/enrollments")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COURSE_FULL"))
                .andExpect(jsonPath("$.data.failureCount").value(1))
                .andExpect(jsonPath("$.data.failedEnrollments[0].lectureId").value(lectureIdLimited))
                .andExpect(jsonPath("$.data.failedEnrollments[0].errorCode").value("COURSE_FULL"));
    }

    @Test
    @DisplayName("내 수강 신청 목록을 조회한다")
    void getMyEnrollments_success() throws Exception {
        enrollmentRepository.save(Enrollment.create(lectureIdA, studentId));
        enrollmentRepository.save(Enrollment.create(lectureIdB, studentId));

        mockMvc.perform(get("/api/enrollments/my")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].lectureId", containsInAnyOrder(
                        lectureIdA.intValue(), lectureIdB.intValue())))
                .andExpect(jsonPath("$.data[*].memberId", everyItem(is(studentId.intValue()))));
    }

    @Test
    @DisplayName("자신의 수강 신청을 취소하면 성공 응답을 반환한다")
    void cancelEnrollment_success() throws Exception {
        Enrollment enrollment = enrollmentRepository.save(Enrollment.create(lectureIdA, studentId));

        mockMvc.perform(delete("/api/enrollments/" + enrollment.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Enrollment updated = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EnrollmentStatus.CANCELED);
    }

    @Test
    @DisplayName("다른 사용자의 수강 신청을 취소하려 하면 403을 반환한다")
    void cancelEnrollment_forbidden() throws Exception {
        Enrollment enrollment = enrollmentRepository.save(Enrollment.create(lectureIdA, otherStudentId));

        mockMvc.perform(delete("/api/enrollments/" + enrollment.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED_ENROLLMENT"));
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 오류를 반환한다")
    void enrollment_withoutToken_unauthorized() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest(List.of(lectureIdA));

        mockMvc.perform(post("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }
}
