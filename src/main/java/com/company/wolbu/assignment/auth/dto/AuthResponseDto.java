package com.company.wolbu.assignment.auth.dto;

import com.company.wolbu.assignment.auth.domain.MemberRole;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDto {
    private Long memberId;
    private String name;
    private String email;
    private String accessToken;
    private MemberRole role;
}


