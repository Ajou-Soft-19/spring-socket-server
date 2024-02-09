package com.ajousw.spring.domain.exception;

public class BadApiResponseException extends RuntimeException {
    public BadApiResponseException(String message) {
        super(message);
    }

    public BadApiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}

