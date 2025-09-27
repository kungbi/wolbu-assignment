package com.company.wolbu.assignment.enrollment.exception;

import com.company.wolbu.assignment.common.exception.NotFoundException;

/**
 * 수강 신청을 찾을 수 없을 때 발생하는 예외
 */
public class EnrollmentNotFoundException extends NotFoundException {

    private static final String ERROR_CODE = "ENROLLMENT_NOT_FOUND";
    private static final String DEFAULT_MESSAGE = "수강 신청을 찾을 수 없습니다.";

    public EnrollmentNotFoundException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public EnrollmentNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    public EnrollmentNotFoundException(Long enrollmentId) {
        super(ERROR_CODE, String.format("수강 신청을 찾을 수 없습니다. (ID: %d)", enrollmentId));
    }

    public EnrollmentNotFoundException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
