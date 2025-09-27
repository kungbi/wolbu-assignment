package com.company.wolbu.assignment.enrollment.exception;

import com.company.wolbu.assignment.common.exception.ConflictException;

/**
 * 이미 수강 신청한 강의에 다시 신청할 때 발생하는 예외
 */
public class AlreadyEnrolledException extends ConflictException {

    private static final String ERROR_CODE = "ALREADY_ENROLLED_ACTIVE";
    private static final String DEFAULT_MESSAGE = "이미 신청한 강의입니다.";

    public AlreadyEnrolledException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public AlreadyEnrolledException(String message) {
        super(ERROR_CODE, message);
    }

    public AlreadyEnrolledException(Long lectureId) {
        super(ERROR_CODE, String.format("이미 신청한 강의입니다. (강의 ID: %d)", lectureId));
    }

    public AlreadyEnrolledException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
