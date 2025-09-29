package com.company.wolbu.assignment.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResultDto {
    private AuthResponseDto response;
    private String refreshToken;
}


