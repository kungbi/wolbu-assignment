package com.company.wolbu.assignment.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 권한이 부족할 때 발생하는 예외
 */
public abstract class ForbiddenException extends BusinessException {

    protected ForbiddenException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.FORBIDDEN);
    }

    protected ForbiddenException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, HttpStatus.FORBIDDEN, cause);
    }
}
