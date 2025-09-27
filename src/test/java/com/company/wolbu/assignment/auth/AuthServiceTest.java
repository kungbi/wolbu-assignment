package com.company.wolbu.assignment.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.dto.AuthResult;
import com.company.wolbu.assignment.auth.dto.LoginRequest;
import com.company.wolbu.assignment.auth.dto.SignUpRequest;
import com.company.wolbu.assignment.auth.dto.SignUpResponse;
import com.company.wolbu.assignment.auth.service.AuthService;
import com.company.wolbu.assignment.common.exception.DomainException;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    AuthService authService;

    @Test
    void signUp_success_and_login_success() {
        SignUpRequest s = new SignUpRequestBuilder()
                .name("홍길동")
                .email("hong@example.com")
                .phone("01012345678")
                .password("Abc123")
                .role(MemberRole.STUDENT)
                .build();

        SignUpResponse signUp = authService.signUp(s);
        assertThat(signUp.getMemberId()).isNotNull();
        assertThat(signUp.getEmail()).isEqualTo("hong@example.com");

        LoginRequest l = new LoginRequestBuilder()
                .email("hong@example.com")
                .password("Abc123")
                .build();
        AuthResult login = authService.login(l);
        assertThat(login.getResponse().getAccessToken()).isNotBlank();
        assertThat(login.getRefreshToken()).isNotBlank();
    }

    @Test
    void signUp_duplicate_email_fails() {
        SignUpRequest s1 = new SignUpRequestBuilder()
                .name("A")
                .email("dup@example.com")
                .phone("01000000000")
                .password("Abc123")
                .role(MemberRole.STUDENT)
                .build();
        authService.signUp(s1);

        SignUpRequest s2 = new SignUpRequestBuilder()
                .name("B")
                .email("dup@example.com")
                .phone("01011111111")
                .password("Abc123")
                .role(MemberRole.STUDENT)
                .build();

        assertThatThrownBy(() -> authService.signUp(s2))
                .isInstanceOf(com.company.wolbu.assignment.auth.exception.DuplicateEmailException.class)
                .hasMessageContaining("이미 가입된 이메일");
    }

    @Test
    void refreshToken_success() {
        // 1. 회원가입
        SignUpRequest s = new SignUpRequestBuilder()
                .name("홍길동")
                .email("hong@example.com")
                .phone("01012345678")
                .password("Abc123")
                .role(MemberRole.STUDENT)
                .build();
        authService.signUp(s);

        // 2. 로그인하여 refresh token 획득
        LoginRequest l = new LoginRequestBuilder()
                .email("hong@example.com")
                .password("Abc123")
                .build();
        AuthResult loginResult = authService.login(l);
        String refreshToken = loginResult.getRefreshToken();

        // 3. refresh token으로 새로운 access token 발급
        AuthResult refreshResult = authService.refreshToken(refreshToken);

        assertThat(refreshResult.getResponse().getAccessToken()).isNotBlank();
        assertThat(refreshResult.getRefreshToken()).isNotBlank();
        assertThat(refreshResult.getResponse().getEmail()).isEqualTo("hong@example.com");
    }

    @Test
    void refreshToken_invalid_token_fails() {
        assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                .isInstanceOf(com.company.wolbu.assignment.auth.exception.TokenExpiredException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰");
    }

    // 테스트 보조 빌더 (DTO는 setter 미노출이므로 빌더로 생성)
    static class SignUpRequestBuilder {
        private final SignUpRequest s = new SignUpRequest();
        SignUpRequestBuilder name(String v) { TestDtoInjector.set(s, "name", v); return this; }
        SignUpRequestBuilder email(String v) { TestDtoInjector.set(s, "email", v); return this; }
        SignUpRequestBuilder phone(String v) { TestDtoInjector.set(s, "phone", v); return this; }
        SignUpRequestBuilder password(String v) { TestDtoInjector.set(s, "password", v); return this; }
        SignUpRequestBuilder role(MemberRole v) { TestDtoInjector.set(s, "role", v); return this; }
        SignUpRequest build() { return s; }
    }

    static class LoginRequestBuilder {
        private final LoginRequest l = new LoginRequest();
        LoginRequestBuilder email(String v) { TestDtoInjector.set(l, "email", v); return this; }
        LoginRequestBuilder password(String v) { TestDtoInjector.set(l, "password", v); return this; }
        LoginRequest build() { return l; }
    }

}


