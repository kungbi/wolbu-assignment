package com.company.wolbu.assignment.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResult {
    private AuthResponse response;
    private String refreshToken;
}


