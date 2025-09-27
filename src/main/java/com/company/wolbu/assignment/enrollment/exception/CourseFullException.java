package com.company.wolbu.assignment.enrollment.exception;

import com.company.wolbu.assignment.common.exception.ConflictException;

/**
 * 강의 정원이 초과되었을 때 발생하는 예외
 */
public class CourseFullException extends ConflictException {

    private static final String ERROR_CODE = "COURSE_FULL";
    private static final String DEFAULT_MESSAGE = "정원이 초과되었습니다.";

    public CourseFullException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public CourseFullException(String message) {
        super(ERROR_CODE, message);
    }

    public CourseFullException(Long lectureId, int maxCapacity) {
        super(ERROR_CODE, String.format("정원이 초과되었습니다. (강의 ID: %d, 최대 정원: %d명)", lectureId, maxCapacity));
    }

    public CourseFullException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
