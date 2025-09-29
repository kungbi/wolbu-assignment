package com.company.wolbu.assignment.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.dto.LoginRequestDto;
import com.company.wolbu.assignment.auth.dto.SignUpRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthController 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 API 성공")
    void signUp_Success() throws Exception {
        // Given
        SignUpRequestDto request = createSignUpRequest("홍길동", "hong@example.com", "01012345678", "Pass123",
                MemberRole.STUDENT);
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.email").value("hong@example.com"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andExpect(jsonPath("$.data.memberId").isNumber());
    }

    @Test
    @DisplayName("회원가입 API - 유효성 검증 실패 (잘못된 이메일)")
    void signUp_ValidationFailed_InvalidEmail() throws Exception {
        // Given
        SignUpRequestDto request = createSignUpRequest("홍길동", "invalid-email", "01012345678", "Pass123",
                MemberRole.STUDENT);
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("회원가입 API - 중복 이메일")
    void signUp_DuplicateEmail() throws Exception {
        // Given
        SignUpRequestDto firstRequest = createSignUpRequest("홍길동", "hong@example.com", "01012345678", "Pass123",
                MemberRole.STUDENT);
        SignUpRequestDto duplicateRequest = createSignUpRequest("김철수", "hong@example.com", "01087654321", "Pass456",
                MemberRole.INSTRUCTOR);

        String firstRequestJson = objectMapper.writeValueAsString(firstRequest);
        String duplicateRequestJson = objectMapper.writeValueAsString(duplicateRequest);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequestJson))
                .andExpect(status().isOk());

        // When & Then - 같은 이메일로 다시 회원가입
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateRequestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("로그인 API 성공")
    void login_Success() throws Exception {
        // Given - 먼저 회원가입
        SignUpRequestDto signUpRequest = createSignUpRequest("홍길동", "hong@example.com", "01012345678", "Pass123",
                MemberRole.STUDENT);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk());

        LoginRequestDto loginRequest = createLoginRequest("hong@example.com", "Pass123");
        String loginJson = objectMapper.writeValueAsString(loginRequest);

        // When
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.email").value("hong@example.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andReturn();

        // Then - 쿠키에 refresh token이 설정되었는지 확인
        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotNull();
        assertThat(cookies).anyMatch(cookie -> cookie.contains("refreshToken="));
        assertThat(cookies).anyMatch(cookie -> cookie.contains("HttpOnly"));
    }

    @Test
    @DisplayName("로그인 API - 잘못된 비밀번호")
    void login_InvalidPassword() throws Exception {
        // Given - 먼저 회원가입
        SignUpRequestDto signUpRequest = createSignUpRequest("홍길동", "hong@example.com", "01012345678", "Pass123",
                MemberRole.STUDENT);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk());

        LoginRequestDto loginRequest = createLoginRequest("hong@example.com", "WrongPassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("로그인 API - 존재하지 않는 이메일")
    void login_EmailNotFound() throws Exception {
        // Given
        LoginRequestDto loginRequest = createLoginRequest("notfound@example.com", "Pass123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("토큰 갱신 API 성공")
    void refreshToken_Success() throws Exception {
        // Given - 회원가입 및 로그인하여 refresh token 획득
        SignUpRequestDto signUpRequest = createSignUpRequest("홍길동", "hong@example.com", "01012345678", "Pass123",
                MemberRole.STUDENT);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk());

        LoginRequestDto loginRequest = createLoginRequest("hong@example.com", "Pass123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String refreshTokenValue = extractRefreshTokenValue(
                loginResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE));
        assertThat(refreshTokenValue).isNotEmpty();

        // When - refresh token으로 새로운 access token 발급
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", refreshTokenValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.email").value("hong@example.com"))
                .andReturn();

        // Then - 새로운 refresh token이 쿠키에 설정되었는지 확인
        List<String> cookies = refreshResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotNull();
        assertThat(cookies).anyMatch(cookie -> cookie.contains("refreshToken="));
    }

    @Test
    @DisplayName("토큰 갱신 API - refresh token 없음")
    void refreshToken_MissingToken() throws Exception {
        // When & Then - refresh token 없이 요청
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("REFRESH_TOKEN_MISSING"))
                .andExpect(jsonPath("$.error.message").value("리프레시 토큰이 없습니다."));
    }

    @Test
    @DisplayName("토큰 갱신 API - 유효하지 않은 refresh token")
    void refreshToken_InvalidToken() throws Exception {
        // Given - 잘못된 refresh token
        Cookie invalidRefreshToken = new Cookie("refreshToken", "invalid-token");

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(invalidRefreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("회원가입 API - 유효성 검증 실패 (빈 이름)")
    void signUp_ValidationFailed_EmptyName() throws Exception {
        // Given
        SignUpRequestDto request = createSignUpRequest("", "hong@example.com", "01012345678", "Pass123",
                MemberRole.STUDENT);

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("로그인 API - 유효성 검증 실패 (빈 이메일)")
    void login_ValidationFailed_EmptyEmail() throws Exception {
        // Given
        LoginRequestDto request = createLoginRequest("", "Pass123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    /**
     * Set-Cookie 헤더에서 refresh token 값 추출
     */
    private String extractRefreshTokenValue(List<String> cookies) {
        if (cookies == null) {
            return null;
        }

        for (String cookie : cookies) {
            if (cookie.startsWith("refreshToken=")) {
                int endIndex = cookie.indexOf(';');
                if (endIndex == -1) {
                    endIndex = cookie.length();
                }
                return cookie.substring("refreshToken=".length(), endIndex);
            }
        }
        return null;
    }

    /**
     * 테스트용 SignUpRequest 생성 헬퍼 메서드
     */
    private SignUpRequestDto createSignUpRequest(String name, String email, String phone, String password,
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
     * 테스트용 LoginRequest 생성 헬퍼 메서드
     */
    private LoginRequestDto createLoginRequest(String email, String password) {
        LoginRequestDto request = new LoginRequestDto();
        TestDtoInjector.set(request, "email", email);
        TestDtoInjector.set(request, "password", password);
        return request;
    }
}
