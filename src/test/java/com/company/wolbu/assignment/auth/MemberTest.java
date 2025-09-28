package com.company.wolbu.assignment.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.company.wolbu.assignment.auth.domain.Member;
import com.company.wolbu.assignment.auth.domain.MemberRole;

/**
 * Member 도메인 단위 테스트
 */
class MemberTest {

    @Test
    @DisplayName("Member 생성 성공 - 학생")
    void createMember_Student_Success() {
        // Given
        String name = "홍길동";
        String email = "hong@example.com";
        String phone = "01012345678";
        String passwordHash = "hashedPassword123";
        MemberRole role = MemberRole.STUDENT;

        // When
        Member member = Member.create(name, email, phone, passwordHash, role);

        // Then
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getPhone()).isEqualTo(phone);
        assertThat(member.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(member.getRole()).isEqualTo(role);
        assertThat(member.getId()).isNull(); // 아직 영속화되지 않음
    }

    @Test
    @DisplayName("Member 생성 성공 - 강사")
    void createMember_Instructor_Success() {
        // Given
        String name = "김교수";
        String email = "kim@example.com";
        String phone = "01087654321";
        String passwordHash = "hashedPassword456";
        MemberRole role = MemberRole.INSTRUCTOR;

        // When
        Member member = Member.create(name, email, phone, passwordHash, role);

        // Then
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getPhone()).isEqualTo(phone);
        assertThat(member.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(member.getRole()).isEqualTo(role);
    }

    @Test
    @DisplayName("학생 역할 확인")
    void isStudent_ReturnsTrue_WhenRoleIsStudent() {
        // Given
        Member student = Member.create("학생", "student@example.com", "01012345678", "hash", MemberRole.STUDENT);
        Member instructor = Member.create("강사", "instructor@example.com", "01087654321", "hash", MemberRole.INSTRUCTOR);

        // When & Then
        assertThat(student.isStudent()).isTrue();
        assertThat(student.isInstructor()).isFalse();

        assertThat(instructor.isStudent()).isFalse();
        assertThat(instructor.isInstructor()).isTrue();
    }

    @Test
    @DisplayName("강사 역할 확인")
    void isInstructor_ReturnsTrue_WhenRoleIsInstructor() {
        // Given
        Member instructor = Member.create("강사", "instructor@example.com", "01087654321", "hash", MemberRole.INSTRUCTOR);
        Member student = Member.create("학생", "student@example.com", "01012345678", "hash", MemberRole.STUDENT);

        // When & Then
        assertThat(instructor.isInstructor()).isTrue();
        assertThat(instructor.isStudent()).isFalse();

        assertThat(student.isInstructor()).isFalse();
        assertThat(student.isStudent()).isTrue();
    }

    @Test
    @DisplayName("동일한 정보로 생성된 Member 객체들은 각각 다른 인스턴스")
    void createMember_DifferentInstances() {
        // Given
        String name = "홍길동";
        String email = "hong@example.com";
        String phone = "01012345678";
        String passwordHash = "hashedPassword";
        MemberRole role = MemberRole.STUDENT;

        // When
        Member member1 = Member.create(name, email, phone, passwordHash, role);
        Member member2 = Member.create(name, email, phone, passwordHash, role);

        // Then
        assertThat(member1).isNotSameAs(member2);
        assertThat(member1.getName()).isEqualTo(member2.getName());
        assertThat(member1.getEmail()).isEqualTo(member2.getEmail());
        assertThat(member1.getPhone()).isEqualTo(member2.getPhone());
        assertThat(member1.getPasswordHash()).isEqualTo(member2.getPasswordHash());
        assertThat(member1.getRole()).isEqualTo(member2.getRole());
    }

    @Test
    @DisplayName("Member 생성 시 모든 필드가 올바르게 설정됨")
    void createMember_AllFieldsSet() {
        // Given
        String name = "테스트사용자";
        String email = "test@example.com";
        String phone = "01011111111";
        String passwordHash = "$2a$10$hashedPasswordExample";
        MemberRole role = MemberRole.INSTRUCTOR;

        // When
        Member member = Member.create(name, email, phone, passwordHash, role);

        // Then
        // 모든 필드가 null이 아닌지 확인
        assertThat(member.getName()).isNotNull().isNotBlank();
        assertThat(member.getEmail()).isNotNull().isNotBlank();
        assertThat(member.getPhone()).isNotNull().isNotBlank();
        assertThat(member.getPasswordHash()).isNotNull().isNotBlank();
        assertThat(member.getRole()).isNotNull();

        // 값이 정확히 설정되었는지 확인
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getPhone()).isEqualTo(phone);
        assertThat(member.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(member.getRole()).isEqualTo(role);
    }
}
