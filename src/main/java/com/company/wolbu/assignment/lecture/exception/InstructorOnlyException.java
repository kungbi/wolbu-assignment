package com.company.wolbu.assignment.lecture.exception;

import com.company.wolbu.assignment.common.exception.ForbiddenException;

/**
 * 강사만 가능한 작업을 일반 회원이 시도할 때 발생하는 예외
 */
public class InstructorOnlyException extends ForbiddenException {

    private static final String ERROR_CODE = "INSTRUCTOR_ONLY";
    private static final String DEFAULT_MESSAGE = "강의는 강사만 개설할 수 있습니다.";

    public InstructorOnlyException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public InstructorOnlyException(String message) {
        super(ERROR_CODE, message);
    }

    public InstructorOnlyException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
