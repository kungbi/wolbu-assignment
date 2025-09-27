package com.company.wolbu.assignment.enrollment.exception;

import com.company.wolbu.assignment.common.exception.ConflictException;

/**
 * 이미 취소된 수강 신청을 다시 취소하려 할 때 발생하는 예외
 */
public class AlreadyCanceledException extends ConflictException {

    private static final String ERROR_CODE = "ALREADY_CANCELED";
    private static final String DEFAULT_MESSAGE = "이미 취소된 수강 신청입니다.";

    public AlreadyCanceledException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public AlreadyCanceledException(String message) {
        super(ERROR_CODE, message);
    }

    public AlreadyCanceledException(Long enrollmentId) {
        super(ERROR_CODE, String.format("이미 취소된 수강 신청입니다. (수강 신청 ID: %d)", enrollmentId));
    }

    public AlreadyCanceledException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
