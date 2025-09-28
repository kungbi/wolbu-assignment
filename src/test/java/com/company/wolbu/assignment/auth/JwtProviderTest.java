package com.company.wolbu.assignment.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.security.JwtProperties;
import com.company.wolbu.assignment.auth.security.JwtProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

/**
 * JwtProvider 단위 테스트
 */
class JwtProviderTest {

    private JwtProvider jwtProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        ReflectionTestUtils.setField(jwtProperties, "secret", "test-secret-key-for-jwt-token-generation-and-validation");
        ReflectionTestUtils.setField(jwtProperties, "accessTtlSeconds", 3600L); // 1시간
        ReflectionTestUtils.setField(jwtProperties, "refreshTtlSeconds", 1209600L); // 14일

        jwtProvider = new JwtProvider(jwtProperties);
    }

    @Test
    @DisplayName("Access Token 생성 성공")
    void generateAccessToken_Success() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        MemberRole role = MemberRole.STUDENT;

        // When
        String token = jwtProvider.generateAccessToken(userId, email, role);

        // Then
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 header.payload.signature 구조
    }

    @Test
    @DisplayName("Refresh Token 생성 성공")
    void generateRefreshToken_Success() {
        // Given
        Long userId = 1L;

        // When
        String token = jwtProvider.generateRefreshToken(userId);

        // Then
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 header.payload.signature 구조
    }

    @Test
    @DisplayName("JWT secret이 null인 경우 예외 발생")
    void generateToken_NullSecret_ThrowsException() {
        // Given
        ReflectionTestUtils.setField(jwtProperties, "secret", null);
        JwtProvider providerWithNullSecret = new JwtProvider(jwtProperties);

        // When & Then
        assertThatThrownBy(() -> providerWithNullSecret.generateAccessToken(1L, "test@example.com", MemberRole.STUDENT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret is not configured");

        assertThatThrownBy(() -> providerWithNullSecret.generateRefreshToken(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret is not configured");
    }

    @Test
    @DisplayName("Access Token 파싱 성공")
    void parseAccessToken_Success() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        MemberRole role = MemberRole.INSTRUCTOR;
        String token = jwtProvider.generateAccessToken(userId, email, role);

        // When
        Claims claims = jwtProvider.parse(token);

        // Then
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(userId));
        assertThat(claims.get("email", String.class)).isEqualTo(email);
        assertThat(claims.get("role", String.class)).isEqualTo(role.name());
        assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("Refresh Token 파싱 성공")
    void parseRefreshToken_Success() {
        // Given
        Long userId = 2L;
        String token = jwtProvider.generateRefreshToken(userId);

        // When
        Claims claims = jwtProvider.parse(token);

        // Then
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(userId));
        assertThat(claims.getId()).isNotBlank(); // UUID가 설정되어 있어야 함
        assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 파싱 시 예외 발생")
    void parse_MalformedToken_ThrowsException() {
        // Given
        String malformedToken = "invalid.token.format";

        // When & Then
        assertThatThrownBy(() -> jwtProvider.parse(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("잘못된 서명의 토큰 파싱 시 예외 발생")
    void parse_InvalidSignature_ThrowsException() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        MemberRole role = MemberRole.STUDENT;
        String validToken = jwtProvider.generateAccessToken(userId, email, role);
        
        // 서명 부분을 임의로 변경
        String[] parts = validToken.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".tampered-signature";

        // When & Then
        assertThatThrownBy(() -> jwtProvider.parse(tamperedToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("만료된 토큰 파싱 시 예외 발생")
    void parse_ExpiredToken_ThrowsException() {
        // Given - TTL을 매우 짧게 설정하여 만료된 토큰 생성
        ReflectionTestUtils.setField(jwtProperties, "accessTtlSeconds", -1L); // 이미 만료됨
        JwtProvider expiredTokenProvider = new JwtProvider(jwtProperties);
        
        String expiredToken = expiredTokenProvider.generateAccessToken(1L, "test@example.com", MemberRole.STUDENT);

        // When & Then
        assertThatThrownBy(() -> jwtProvider.parse(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("파싱 시 JWT secret이 null인 경우 예외 발생")
    void parse_NullSecret_ThrowsException() {
        // Given
        String token = jwtProvider.generateAccessToken(1L, "test@example.com", MemberRole.STUDENT);
        
        ReflectionTestUtils.setField(jwtProperties, "secret", null);
        JwtProvider providerWithNullSecret = new JwtProvider(jwtProperties);

        // When & Then
        assertThatThrownBy(() -> providerWithNullSecret.parse(token))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret is not configured");
    }

    @Test
    @DisplayName("동일한 사용자로 생성한 여러 Refresh Token은 서로 다른 ID를 가짐")
    void generateRefreshToken_DifferentIds() {
        // Given
        Long userId = 1L;

        // When
        String token1 = jwtProvider.generateRefreshToken(userId);
        String token2 = jwtProvider.generateRefreshToken(userId);

        Claims claims1 = jwtProvider.parse(token1);
        Claims claims2 = jwtProvider.parse(token2);

        // Then
        assertThat(claims1.getId()).isNotEqualTo(claims2.getId());
        assertThat(claims1.getSubject()).isEqualTo(claims2.getSubject()); // userId는 동일
    }

    @Test
    @DisplayName("Access Token에는 ID가 없고, Refresh Token에는 ID가 있음")
    void tokenStructureDifference() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        MemberRole role = MemberRole.STUDENT;

        // When
        String accessToken = jwtProvider.generateAccessToken(userId, email, role);
        String refreshToken = jwtProvider.generateRefreshToken(userId);

        Claims accessClaims = jwtProvider.parse(accessToken);
        Claims refreshClaims = jwtProvider.parse(refreshToken);

        // Then
        // Access Token: email과 role 정보 포함, ID 없음
        assertThat(accessClaims.get("email")).isEqualTo(email);
        assertThat(accessClaims.get("role")).isEqualTo(role.name());
        assertThat(accessClaims.getId()).isNull();

        // Refresh Token: email과 role 정보 없음, ID 있음
        assertThat(refreshClaims.get("email")).isNull();
        assertThat(refreshClaims.get("role")).isNull();
        assertThat(refreshClaims.getId()).isNotNull();
    }
}
