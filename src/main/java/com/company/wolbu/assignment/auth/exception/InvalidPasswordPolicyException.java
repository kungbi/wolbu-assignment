package com.company.wolbu.assignment.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * 비밀번호 정책 위반 시 발생하는 예외
 */
public class InvalidPasswordPolicyException extends AuthException {

    private static final String ERROR_CODE = "INVALID_PASSWORD_POLICY";
    private static final String DEFAULT_MESSAGE = "비밀번호 정책을 만족하지 않습니다.";

    public InvalidPasswordPolicyException() {
        super(ERROR_CODE, DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    public InvalidPasswordPolicyException(String message) {
        super(ERROR_CODE, message, HttpStatus.BAD_REQUEST);
    }

    public InvalidPasswordPolicyException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST, cause);
    }
}
