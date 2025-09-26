package com.company.wolbu.assignment.auth.dto;

import com.company.wolbu.assignment.auth.domain.MemberRole;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpResponse {
    private Long memberId;
    private String name;
    private String email;
    private MemberRole role;
}


