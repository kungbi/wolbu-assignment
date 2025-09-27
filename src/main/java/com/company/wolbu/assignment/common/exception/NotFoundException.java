package com.company.wolbu.assignment.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
public abstract class NotFoundException extends BusinessException {

    protected NotFoundException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.NOT_FOUND);
    }

    protected NotFoundException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, HttpStatus.NOT_FOUND, cause);
    }
}
