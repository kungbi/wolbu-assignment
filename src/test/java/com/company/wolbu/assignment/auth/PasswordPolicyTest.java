package com.company.wolbu.assignment.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.company.wolbu.assignment.auth.security.PasswordPolicy;

class PasswordPolicyTest {

    @Test
    void valid_passwords() {
        assertThat(PasswordPolicy.isValid("Abc123")).isTrue();
        assertThat(PasswordPolicy.isValid("abc123"))
            .as("소문자+숫자 조합")
            .isTrue();
        assertThat(PasswordPolicy.isValid("ABC123"))
            .as("대문자+숫자 조합")
            .isTrue();
        assertThat(PasswordPolicy.isValid("Abcdef"))
            .as("대소문자 조합")
            .isTrue();
    }

    @Test
    void invalid_passwords() {
        assertThat(PasswordPolicy.isValid("abc"))
            .as("너무 짧음")
            .isFalse();
        assertThat(PasswordPolicy.isValid("abcdefghijk"))
            .as("너무 김")
            .isFalse();
        assertThat(PasswordPolicy.isValid("abcdef"))
            .as("한 종류만")
            .isFalse();
    }
}


