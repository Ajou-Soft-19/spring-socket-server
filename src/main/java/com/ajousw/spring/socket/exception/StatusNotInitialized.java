package com.ajousw.spring.socket.exception;

public class StatusNotInitialized extends RuntimeException {
    public StatusNotInitialized(String message) {
        super(message);
    }

    public StatusNotInitialized(String message, Throwable cause) {
        super(message, cause);
    }
}
