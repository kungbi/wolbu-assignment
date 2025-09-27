package com.company.wolbu.assignment.auth.exception;

import com.company.wolbu.assignment.common.exception.ConflictException;

/**
 * 이메일 중복 시 발생하는 예외
 */
public class DuplicateEmailException extends ConflictException {

    private static final String ERROR_CODE = "EMAIL_DUPLICATE";
    private static final String DEFAULT_MESSAGE = "이미 사용 중인 이메일입니다.";

    public DuplicateEmailException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public DuplicateEmailException(String message) {
        super(ERROR_CODE, message);
    }

    public DuplicateEmailException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
