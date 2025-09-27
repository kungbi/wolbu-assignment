package com.company.wolbu.assignment.enrollment.exception;

import org.springframework.http.HttpStatus;

import com.company.wolbu.assignment.common.exception.BusinessException;

/**
 * 수강 신청 도메인의 기본 예외 클래스
 */
public abstract class EnrollmentException extends BusinessException {

    protected EnrollmentException(String errorCode, String message, HttpStatus httpStatus) {
        super(errorCode, message, httpStatus);
    }

    protected EnrollmentException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(errorCode, message, httpStatus, cause);
    }
}
