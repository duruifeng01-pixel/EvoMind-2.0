package com.evomind.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = 400;
    }

    public BusinessException(String message, Integer errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 500;
    }
}
