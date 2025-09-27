package com.company.wolbu.assignment.lecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.company.wolbu.assignment.auth.domain.Member;
import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.repository.MemberRepository;
import com.company.wolbu.assignment.common.exception.DomainException;
import com.company.wolbu.assignment.lecture.dto.CreateLectureRequest;
import com.company.wolbu.assignment.lecture.dto.CreateLectureResponse;
import com.company.wolbu.assignment.lecture.service.LectureService;

/**
 * 강의 서비스 테스트
 */
@SpringBootTest
@Transactional
class LectureServiceTest {

    @Autowired
    private LectureService lectureService;
    
    @Autowired
    private MemberRepository memberRepository;
    
    private Long instructorId;
    private Long studentId;

    @BeforeEach
    void setUp() {
        // 테스트용 강사 생성
        Member instructor = Member.create("강사", "instructor@example.com", "010-1111-1111", "hashedPassword", MemberRole.INSTRUCTOR);
        Member savedInstructor = memberRepository.save(instructor);
        instructorId = savedInstructor.getId();
        
        // 테스트용 수강생 생성
        Member student = Member.create("수강생", "student@example.com", "010-2222-2222", "hashedPassword", MemberRole.STUDENT);
        Member savedStudent = memberRepository.save(student);
        studentId = savedStudent.getId();
    }

    @Test
    @DisplayName("강사가 강의를 성공적으로 개설한다")
    void createLecture_instructor_success() {
        // Given
        CreateLectureRequest request = new CreateLectureRequest("내집마련 기초반", 10, 200000);
        
        // When
        CreateLectureResponse response = lectureService.createLecture(instructorId, request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("내집마련 기초반");
        assertThat(response.getMaxCapacity()).isEqualTo(10);
        assertThat(response.getPrice()).isEqualTo(200000);
        assertThat(response.getInstructorId()).isEqualTo(instructorId);
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("수강생은 강의를 개설할 수 없다")
    void createLecture_student_fails() {
        // Given
        CreateLectureRequest request = new CreateLectureRequest("내집마련 기초반", 10, 200000);
        
        // When & Then
        assertThatThrownBy(() -> lectureService.createLecture(studentId, request))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "INSTRUCTOR_ONLY")
                .hasMessageContaining("강의는 강사만 개설할 수 있습니다");
    }

    @Test
    @DisplayName("존재하지 않는 회원은 강의를 개설할 수 없다")
    void createLecture_nonexistent_member_fails() {
        // Given
        Long nonExistentMemberId = 99999L;
        CreateLectureRequest request = new CreateLectureRequest("내집마련 기초반", 10, 200000);
        
        // When & Then
        assertThatThrownBy(() -> lectureService.createLecture(nonExistentMemberId, request))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "MEMBER_NOT_FOUND")
                .hasMessageContaining("회원을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("유효하지 않은 강의 정보로는 강의를 개설할 수 없다")
    void createLecture_invalid_data_fails() {
        // Given - 빈 제목
        CreateLectureRequest requestWithEmptyTitle = new CreateLectureRequest("", 10, 200000);
        
        // When & Then
        assertThatThrownBy(() -> lectureService.createLecture(instructorId, requestWithEmptyTitle))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_LECTURE_DATA");

        // Given - 0명 정원
        CreateLectureRequest requestWithZeroCapacity = new CreateLectureRequest("강의명", 0, 200000);
        
        // When & Then
        assertThatThrownBy(() -> lectureService.createLecture(instructorId, requestWithZeroCapacity))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_LECTURE_DATA");

        // Given - 음수 가격
        CreateLectureRequest requestWithNegativePrice = new CreateLectureRequest("강의명", 10, -1000);
        
        // When & Then
        assertThatThrownBy(() -> lectureService.createLecture(instructorId, requestWithNegativePrice))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_LECTURE_DATA");
    }

    @Test
    @DisplayName("강의 정보를 성공적으로 조회한다")
    void getLecture_success() {
        // Given
        CreateLectureRequest request = new CreateLectureRequest("내집마련 기초반", 10, 200000);
        CreateLectureResponse createdLecture = lectureService.createLecture(instructorId, request);
        
        // When
        CreateLectureResponse response = lectureService.getLecture(createdLecture.getId());
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(createdLecture.getId());
        assertThat(response.getTitle()).isEqualTo("내집마련 기초반");
        assertThat(response.getMaxCapacity()).isEqualTo(10);
        assertThat(response.getPrice()).isEqualTo(200000);
        assertThat(response.getInstructorId()).isEqualTo(instructorId);
    }

    @Test
    @DisplayName("존재하지 않는 강의는 조회할 수 없다")
    void getLecture_not_found_fails() {
        // Given
        Long nonExistentLectureId = 99999L;
        
        // When & Then
        assertThatThrownBy(() -> lectureService.getLecture(nonExistentLectureId))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "LECTURE_NOT_FOUND")
                .hasMessageContaining("강의를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("강사의 강의 권한을 확인한다")
    void hasInstructorPermission_success() {
        // Given
        CreateLectureRequest request = new CreateLectureRequest("내집마련 기초반", 10, 200000);
        CreateLectureResponse createdLecture = lectureService.createLecture(instructorId, request);
        
        // When & Then
        assertThat(lectureService.hasInstructorPermission(createdLecture.getId(), instructorId)).isTrue();
        assertThat(lectureService.hasInstructorPermission(createdLecture.getId(), studentId)).isFalse();
    }
}
