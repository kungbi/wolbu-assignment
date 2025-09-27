package com.company.wolbu.assignment.auth.dto;

import com.company.wolbu.assignment.auth.domain.MemberRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "회원가입 요청")
public class SignUpRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 20, message = "이름은 20자 이하로 입력해주세요.")
    @Schema(description = "회원 이름", example = "홍길동", maxLength = 20)
    private String name;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "이메일은 100자 이하로 입력해주세요.")
    @Schema(description = "이메일 주소", example = "hong@example.com", maxLength = 100)
    private String email;

    @NotBlank(message = "휴대폰 번호를 입력해주세요.")
    @Pattern(regexp = "^010\\d{8}$", message = "휴대폰 번호는 010으로 시작하는 11자리 숫자여야 합니다.")
    @Schema(description = "휴대폰 번호", example = "01012345678", maxLength = 11)
    private String phone;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 6, max = 10, message = "비밀번호는 6~10자로 입력해주세요.")
    @Pattern(
        regexp = "^(?:(?=.*[a-z])(?=.*[A-Z])|(?=.*[a-z])(?=.*[0-9])|(?=.*[A-Z])(?=.*[0-9])).{6,10}$",
        message = "비밀번호는 영문 대소문자와 숫자 중 2종 이상 조합이어야 합니다."
    )
    @Schema(description = "비밀번호 (6~10자, 영문 대소문자와 숫자 중 2종 이상 조합)", example = "Password123!", minLength = 6, maxLength = 10)
    private String password;

    @NotNull(message = "회원 유형을 선택해주세요.")
    @Schema(description = "회원 유형", example = "STUDENT", implementation = MemberRole.class)
    private MemberRole role;
}


