package com.company.wolbu.assignment.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 리소스 충돌 시 발생하는 예외
 */
public abstract class ConflictException extends BusinessException {

    protected ConflictException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.CONFLICT);
    }

    protected ConflictException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, HttpStatus.CONFLICT, cause);
    }
}
