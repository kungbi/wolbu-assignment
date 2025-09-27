package com.company.wolbu.assignment.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 인증이 필요하거나 인증에 실패했을 때 발생하는 예외
 */
public abstract class UnauthorizedException extends BusinessException {

    protected UnauthorizedException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.UNAUTHORIZED);
    }

    protected UnauthorizedException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, HttpStatus.UNAUTHORIZED, cause);
    }
}
