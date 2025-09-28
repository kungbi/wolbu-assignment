package com.company.wolbu.assignment.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.company.wolbu.assignment.auth.domain.Member;
import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.domain.RefreshToken;

/**
 * RefreshToken 도메인 단위 테스트
 */
class RefreshTokenTest {

    @Test
    @DisplayName("RefreshToken 생성 성공")
    void issueRefreshToken_Success() {
        // Given
        Member member = Member.create("홍길동", "hong@example.com", "01012345678", "hashedPassword", MemberRole.STUDENT);
        String tokenValue = "refresh-token-value-example";

        // When
        RefreshToken refreshToken = RefreshToken.issue(member, tokenValue);

        // Then
        assertThat(refreshToken.getMember()).isSameAs(member);
        assertThat(refreshToken.getToken()).isEqualTo(tokenValue);
        assertThat(refreshToken.getId()).isNull(); // 아직 영속화되지 않음
    }

    @Test
    @DisplayName("동일한 Member로 여러 RefreshToken 생성 가능")
    void issueMultipleRefreshTokens_SameMember() {
        // Given
        Member member = Member.create("홍길동", "hong@example.com", "01012345678", "hashedPassword", MemberRole.STUDENT);
        String tokenValue1 = "refresh-token-1";
        String tokenValue2 = "refresh-token-2";

        // When
        RefreshToken refreshToken1 = RefreshToken.issue(member, tokenValue1);
        RefreshToken refreshToken2 = RefreshToken.issue(member, tokenValue2);

        // Then
        assertThat(refreshToken1).isNotSameAs(refreshToken2);
        assertThat(refreshToken1.getMember()).isSameAs(member);
        assertThat(refreshToken2.getMember()).isSameAs(member);
        assertThat(refreshToken1.getToken()).isEqualTo(tokenValue1);
        assertThat(refreshToken2.getToken()).isEqualTo(tokenValue2);
        assertThat(refreshToken1.getToken()).isNotEqualTo(refreshToken2.getToken());
    }

    @Test
    @DisplayName("서로 다른 Member로 RefreshToken 생성")
    void issueRefreshTokens_DifferentMembers() {
        // Given
        Member student = Member.create("학생", "student@example.com", "01012345678", "hash1", MemberRole.STUDENT);
        Member instructor = Member.create("강사", "instructor@example.com", "01087654321", "hash2", MemberRole.INSTRUCTOR);
        String tokenValue = "same-token-value";

        // When
        RefreshToken studentToken = RefreshToken.issue(student, tokenValue);
        RefreshToken instructorToken = RefreshToken.issue(instructor, tokenValue);

        // Then
        assertThat(studentToken).isNotSameAs(instructorToken);
        assertThat(studentToken.getMember()).isSameAs(student);
        assertThat(instructorToken.getMember()).isSameAs(instructor);
        assertThat(studentToken.getMember()).isNotSameAs(instructorToken.getMember());
        
        // 토큰 값은 동일할 수 있음 (실제로는 유니크해야 하지만 도메인 레벨에서는 허용)
        assertThat(studentToken.getToken()).isEqualTo(instructorToken.getToken());
    }

    @Test
    @DisplayName("RefreshToken 생성 시 모든 필드가 올바르게 설정됨")
    void issueRefreshToken_AllFieldsSet() {
        // Given
        Member member = Member.create("테스트사용자", "test@example.com", "01011111111", "hashedPassword", MemberRole.INSTRUCTOR);
        String tokenValue = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.example.token";

        // When
        RefreshToken refreshToken = RefreshToken.issue(member, tokenValue);

        // Then
        // 모든 필드가 null이 아닌지 확인
        assertThat(refreshToken.getMember()).isNotNull();
        assertThat(refreshToken.getToken()).isNotNull().isNotBlank();

        // 값이 정확히 설정되었는지 확인
        assertThat(refreshToken.getMember()).isSameAs(member);
        assertThat(refreshToken.getToken()).isEqualTo(tokenValue);
        
        // Member의 정보도 올바른지 확인
        assertThat(refreshToken.getMember().getName()).isEqualTo("테스트사용자");
        assertThat(refreshToken.getMember().getEmail()).isEqualTo("test@example.com");
        assertThat(refreshToken.getMember().getRole()).isEqualTo(MemberRole.INSTRUCTOR);
    }

    @Test
    @DisplayName("긴 토큰 값으로 RefreshToken 생성")
    void issueRefreshToken_LongTokenValue() {
        // Given
        Member member = Member.create("홍길동", "hong@example.com", "01012345678", "hashedPassword", MemberRole.STUDENT);
        String longTokenValue = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // When
        RefreshToken refreshToken = RefreshToken.issue(member, longTokenValue);

        // Then
        assertThat(refreshToken.getToken()).isEqualTo(longTokenValue);
        assertThat(refreshToken.getToken().length()).isGreaterThan(100); // 긴 토큰 값 확인
        assertThat(refreshToken.getMember()).isSameAs(member);
    }
}
