package com.company.wolbu.assignment.auth.exception;

import com.company.wolbu.assignment.common.exception.UnauthorizedException;

/**
 * 토큰이 만료되었을 때 발생하는 예외
 */
public class TokenExpiredException extends UnauthorizedException {

    private static final String ERROR_CODE = "REFRESH_TOKEN_EXPIRED";
    private static final String DEFAULT_MESSAGE = "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.";

    public TokenExpiredException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public TokenExpiredException(String message) {
        super(ERROR_CODE, message);
    }

    public TokenExpiredException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
