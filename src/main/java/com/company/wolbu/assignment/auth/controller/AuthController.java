package com.company.wolbu.assignment.auth.controller;

import com.company.wolbu.assignment.auth.dto.AuthResponseDto;
import com.company.wolbu.assignment.auth.dto.AuthResultDto;
import com.company.wolbu.assignment.auth.dto.LoginRequestDto;
import com.company.wolbu.assignment.auth.dto.SignUpRequestDto;
import com.company.wolbu.assignment.auth.dto.SignUpResponseDto;
import com.company.wolbu.assignment.auth.service.AuthService;
import com.company.wolbu.assignment.common.dto.ApiResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "회원가입, 로그인, 토큰 갱신을 위한 API")
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "회원가입",
        description = "새로운 회원을 등록합니다. 강사 또는 수강생으로 가입할 수 있습니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원가입 정보",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SignUpRequestDto.class),
                examples = @ExampleObject(
                    name = "회원가입 예시",
                    value = """
                        {
                            "name": "홍길동",
                            "email": "hong@example.com",
                            "phone": "01012345678",
                            "password": "Password123!",
                            "role": "STUDENT"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<SignUpResponseDto>> signUp(@Valid @RequestBody SignUpRequestDto request) {
        SignUpResponseDto result = authService.signUp(request);
        return ResponseEntity.ok(ApiResponseDto.success(result));
    }

    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인합니다. 성공 시 Access Token과 Refresh Token을 반환합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "로그인 정보",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequestDto.class),
                examples = @ExampleObject(
                    name = "로그인 예시",
                    value = """
                        {
                            "email": "hong@example.com",
                            "password": "Password123!"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        AuthResultDto result = authService.login(request);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponseDto.success(result.getResponse()));
    }

    @Operation(
        summary = "토큰 갱신",
        description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다. Refresh Token은 쿠키에서 자동으로 읽습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Refresh Token 누락"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> refreshToken(HttpServletRequest request) {
        // 쿠키에서 refresh token 읽기
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        
        if (refreshToken == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.failure("REFRESH_TOKEN_MISSING", "리프레시 토큰이 없습니다."));
        }
        
        AuthResultDto result = authService.refreshToken(refreshToken);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponseDto.success(result.getResponse()));
    }

}


