package com.company.wolbu.assignment.auth.exception;

import com.company.wolbu.assignment.common.exception.UnauthorizedException;

/**
 * 유효하지 않은 토큰일 때 발생하는 예외
 */
public class InvalidTokenException extends UnauthorizedException {

    private static final String ERROR_CODE = "INVALID_TOKEN";
    private static final String DEFAULT_MESSAGE = "유효하지 않은 인증 토큰입니다.";

    public InvalidTokenException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public InvalidTokenException(String message) {
        super(ERROR_CODE, message);
    }

    public InvalidTokenException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
