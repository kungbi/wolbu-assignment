package com.company.wolbu.assignment.enrollment.exception;

import com.company.wolbu.assignment.common.exception.ForbiddenException;

/**
 * 본인의 수강 신청이 아닌 것을 조작하려 할 때 발생하는 예외
 */
public class UnauthorizedEnrollmentException extends ForbiddenException {

    private static final String ERROR_CODE = "UNAUTHORIZED_ENROLLMENT";
    private static final String DEFAULT_MESSAGE = "본인의 수강 신청만 취소할 수 있습니다.";

    public UnauthorizedEnrollmentException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public UnauthorizedEnrollmentException(String message) {
        super(ERROR_CODE, message);
    }

    public UnauthorizedEnrollmentException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
