package com.company.wolbu.assignment.auth.dto;

import com.company.wolbu.assignment.auth.domain.MemberRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignUpRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    private String name;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 120, message = "이메일은 120자 이하로 입력해주세요.")
    private String email;

    @NotBlank(message = "휴대폰 번호를 입력해주세요.")
    @Size(max = 20, message = "휴대폰 번호는 20자 이하로 입력해주세요.")
    private String phone;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 6, max = 10, message = "비밀번호는 6~10자로 입력해주세요.")
    @Pattern(
        regexp = "^(?:(?=.*[a-z])(?=.*[A-Z])|(?=.*[a-z])(?=.*[0-9])|(?=.*[A-Z])(?=.*[0-9])).{6,10}$",
        message = "비밀번호는 영문 대소문자와 숫자 중 2종 이상 조합이어야 합니다."
    )
    private String password;

    @NotNull(message = "회원 유형을 선택해주세요.")
    private MemberRole role;
}


