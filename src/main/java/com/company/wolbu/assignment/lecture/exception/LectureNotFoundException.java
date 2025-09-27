package com.company.wolbu.assignment.lecture.exception;

import com.company.wolbu.assignment.common.exception.NotFoundException;

/**
 * 강의를 찾을 수 없을 때 발생하는 예외
 */
public class LectureNotFoundException extends NotFoundException {

    private static final String ERROR_CODE = "LECTURE_NOT_FOUND";
    private static final String DEFAULT_MESSAGE = "강의를 찾을 수 없습니다.";

    public LectureNotFoundException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public LectureNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    public LectureNotFoundException(Long lectureId) {
        super(ERROR_CODE, String.format("강의를 찾을 수 없습니다. (ID: %d)", lectureId));
    }

    public LectureNotFoundException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, cause);
    }
}
