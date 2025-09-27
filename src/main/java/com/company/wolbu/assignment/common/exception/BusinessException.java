package com.company.wolbu.assignment.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 예외의 최상위 클래스
 * 모든 도메인 예외는 이 클래스를 상속받습니다.
 */
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    protected BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected BusinessException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * 에러 코드를 반환합니다.
     * @return 에러 코드
     */
    public String errorCode() {
        return errorCode;
    }

    /**
     * HTTP 상태 코드를 반환합니다.
     * @return HTTP 상태 코드
     */
    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
