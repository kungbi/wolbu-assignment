package com.company.wolbu.assignment.exception;

/**
 * 도메인/비즈니스 예외
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


