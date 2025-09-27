package com.company.wolbu.assignment.common.exception;

/**
 * 도메인/비즈니스 예외
 * 공통 예외 클래스로 비즈니스 규칙 위반 시 사용됩니다.
 */
public class DomainException extends RuntimeException {

    private final String code;

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
