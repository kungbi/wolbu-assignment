package com.company.wolbu.assignment.auth.exception;

import org.springframework.http.HttpStatus;

import com.company.wolbu.assignment.common.exception.BusinessException;

/**
 * 인증/인가 도메인의 기본 예외 클래스
 */
public abstract class AuthException extends BusinessException {

    protected AuthException(String errorCode, String message, HttpStatus httpStatus) {
        super(errorCode, message, httpStatus);
    }

    protected AuthException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(errorCode, message, httpStatus, cause);
    }
}
