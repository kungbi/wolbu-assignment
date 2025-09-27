package com.company.wolbu.assignment.enrollment.exception;

import com.company.wolbu.assignment.common.exception.NotFoundException;

/**
 * 회원을 찾을 수 없을 때 발생하는 예외
 */
public class MemberNotFoundException extends NotFoundException {

    private static final String ERROR_CODE = "MEMBER_NOT_FOUND";
    private static final String DEFAULT_MESSAGE = "회원을 찾을 수 없습니다.";

    public MemberNotFoundException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public MemberNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    public MemberNotFoundException(Long memberId) {
        super(ERROR_CODE, String.format("회원을 찾을 수 없습니다. (ID: %d)", memberId));
    }

    public MemberNotFoundException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
