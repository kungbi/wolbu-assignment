package com.company.wolbu.assignment.auth.exception;

import com.company.wolbu.assignment.common.exception.UnauthorizedException;

/**
 * 로그인 실패 시 발생하는 예외
 */
public class InvalidCredentialsException extends UnauthorizedException {

    private static final String ERROR_CODE = "LOGIN_FAILED";
    private static final String DEFAULT_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다.";

    public InvalidCredentialsException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public InvalidCredentialsException(String message) {
        super(ERROR_CODE, message);
    }

    public InvalidCredentialsException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
