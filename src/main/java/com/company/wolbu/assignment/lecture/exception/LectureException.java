package com.company.wolbu.assignment.lecture.exception;

import org.springframework.http.HttpStatus;

import com.company.wolbu.assignment.common.exception.BusinessException;

/**
 * 강의 도메인의 기본 예외 클래스
 */
public abstract class LectureException extends BusinessException {

    protected LectureException(String errorCode, String message, HttpStatus httpStatus) {
        super(errorCode, message, httpStatus);
    }

    protected LectureException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(errorCode, message, httpStatus, cause);
    }
}
