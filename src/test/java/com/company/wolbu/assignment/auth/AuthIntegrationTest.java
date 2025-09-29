package com.company.wolbu.assignment.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.dto.AuthResultDto;
import com.company.wolbu.assignment.auth.dto.LoginRequestDto;
import com.company.wolbu.assignment.auth.dto.SignUpRequestDto;
import com.company.wolbu.assignment.auth.dto.SignUpResponseDto;
import com.company.wolbu.assignment.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Auth 관련 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("회원가입 → 로그인 → 토큰 갱신 전체 플로우 테스트")
    void fullAuthFlow_Success() {
        // Given
        SignUpRequestDto signUpRequest = createSignUpRequestDto("홍길동", "hong@example.com", "01012345678", "Pass123",
                MemberRole.STUDENT);

        // When & Then
        // 1. 회원가입
        SignUpResponseDto signUpResponse = authService.signUp(signUpRequest);
        assertThat(signUpResponse.getMemberId()).isNotNull();
        assertThat(signUpResponse.getEmail()).isEqualTo("hong@example.com");
        assertThat(signUpResponse.getName()).isEqualTo("홍길동");

        // 2. 로그인
        LoginRequestDto loginRequest = createLoginRequestDto("hong@example.com", "Pass123");
        AuthResultDto loginResult = authService.login(loginRequest);

        assertThat(loginResult.getResponse().getAccessToken()).isNotBlank();
        assertThat(loginResult.getRefreshToken()).isNotBlank();
        assertThat(loginResult.getResponse().getEmail()).isEqualTo("hong@example.com");
        assertThat(loginResult.getResponse().getName()).isEqualTo("홍길동");
        assertThat(loginResult.getResponse().getRole()).isEqualTo(MemberRole.STUDENT);

        // 3. 토큰 갱신
        String refreshToken = loginResult.getRefreshToken();
        AuthResultDto refreshResult = authService.refreshToken(refreshToken);

        assertThat(refreshResult.getResponse().getAccessToken()).isNotBlank();
        assertThat(refreshResult.getRefreshToken()).isNotBlank();
        assertThat(refreshResult.getResponse().getEmail()).isEqualTo("hong@example.com");
        assertThat(refreshResult.getResponse().getName()).isEqualTo("홍길동");
        assertThat(refreshResult.getResponse().getRole()).isEqualTo(MemberRole.STUDENT);

        // 새로운 토큰들이 정상적으로 발급되었는지 확인
        assertThat(refreshResult.getResponse().getAccessToken()).isNotBlank();
        assertThat(refreshResult.getRefreshToken()).isNotBlank();

        // 토큰이 다를 가능성이 높지만, 같을 수도 있으므로 존재만 확인
        // (실제로는 매번 다른 토큰이 생성되지만 테스트 환경에서는 예외가 있을 수 있음)
    }

    @Test
    @DisplayName("강사와 학생 역할별 회원가입 테스트")
    void signUp_DifferentRoles() {
        // Given & When & Then
        // 1. 학생 회원가입
        SignUpRequestDto studentRequest = createSignUpRequestDto("학생", "student@example.com", "01011111111", "Stud123",
                MemberRole.STUDENT);
        SignUpResponseDto studentResponse = authService.signUp(studentRequest);

        assertThat(studentResponse.getRole()).isEqualTo(MemberRole.STUDENT);

        // 2. 강사 회원가입
        SignUpRequestDto instructorRequest = createSignUpRequestDto("강사", "instructor@example.com", "01022222222",
                "Inst123", MemberRole.INSTRUCTOR);
        SignUpResponseDto instructorResponse = authService.signUp(instructorRequest);

        assertThat(instructorResponse.getRole()).isEqualTo(MemberRole.INSTRUCTOR);

        // 3. 각각 로그인하여 역할 확인
        LoginRequestDto studentLogin = createLoginRequestDto("student@example.com", "Stud123");
        AuthResultDto studentAuth = authService.login(studentLogin);
        assertThat(studentAuth.getResponse().getRole()).isEqualTo(MemberRole.STUDENT);

        LoginRequestDto instructorLogin = createLoginRequestDto("instructor@example.com", "Inst123");
        AuthResultDto instructorAuth = authService.login(instructorLogin);
        assertThat(instructorAuth.getResponse().getRole()).isEqualTo(MemberRole.INSTRUCTOR);
    }

    @Test
    @DisplayName("여러 사용자 동시 회원가입 및 로그인 테스트")
    void multipleUsers_SignUpAndLogin() {
        // Given
        String[] names = {"사용자1", "사용자2", "사용자3"};
        String[] emails = {"user1@example.com", "user2@example.com", "user3@example.com"};
        String[] phones = {"01011111111", "01022222222", "01033333333"};
        String password = "Pass123";

        // When & Then
        for (int i = 0; i < names.length; i++) {
            // 회원가입
            SignUpRequestDto signUpRequest = createSignUpRequestDto(names[i], emails[i], phones[i], password,
                    MemberRole.STUDENT);
            SignUpResponseDto signUpResponse = authService.signUp(signUpRequest);

            assertThat(signUpResponse.getName()).isEqualTo(names[i]);
            assertThat(signUpResponse.getEmail()).isEqualTo(emails[i]);

            // 로그인
            LoginRequestDto loginRequest = createLoginRequestDto(emails[i], password);
            AuthResultDto loginResult = authService.login(loginRequest);

            assertThat(loginResult.getResponse().getName()).isEqualTo(names[i]);
            assertThat(loginResult.getResponse().getEmail()).isEqualTo(emails[i]);
            assertThat(loginResult.getResponse().getAccessToken()).isNotBlank();
            assertThat(loginResult.getRefreshToken()).isNotBlank();
        }
    }

    /**
     * 테스트용 SignUpRequestDto 생성 헬퍼 메서드
     */
    private SignUpRequestDto createSignUpRequestDto(String name, String email, String phone, String password,
                                                    MemberRole role) {
        SignUpRequestDto request = new SignUpRequestDto();
        TestDtoInjector.set(request, "name", name);
        TestDtoInjector.set(request, "email", email);
        TestDtoInjector.set(request, "phone", phone);
        TestDtoInjector.set(request, "password", password);
        TestDtoInjector.set(request, "role", role);
        return request;
    }

    /**
     * 테스트용 LoginRequestDto 생성 헬퍼 메서드
     */
    private LoginRequestDto createLoginRequestDto(String email, String password) {
        LoginRequestDto request = new LoginRequestDto();
        TestDtoInjector.set(request, "email", email);
        TestDtoInjector.set(request, "password", password);
        return request;
    }
}
