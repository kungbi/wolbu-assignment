package com.company.wolbu.assignment.lecture.exception;

import org.springframework.http.HttpStatus;

/**
 * 강의 데이터가 유효하지 않을 때 발생하는 예외
 */
public class InvalidLectureDataException extends LectureException {

    private static final String ERROR_CODE = "INVALID_LECTURE_DATA";
    private static final String DEFAULT_MESSAGE = "강의 정보가 올바르지 않습니다.";

    public InvalidLectureDataException() {
        super(ERROR_CODE, DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    public InvalidLectureDataException(String message) {
        super(ERROR_CODE, message, HttpStatus.BAD_REQUEST);
    }

    public InvalidLectureDataException(Throwable cause) {
        super(ERROR_CODE, DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST, cause);
    }
}
