package com.company.wolbu.assignment.auth.exception;

import com.company.wolbu.assignment.common.exception.ForbiddenException;

/**
 * 권한이 부족할 때 발생하는 예외
 */
public class InsufficientRoleException extends ForbiddenException {

    private static final String ERROR_CODE = "INSUFFICIENT_ROLE";
    private static final String DEFAULT_MESSAGE = "해당 작업을 수행할 권한이 없습니다.";

    public InsufficientRoleException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public InsufficientRoleException(String message) {
        super(ERROR_CODE, message);
    }

    public InsufficientRoleException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
