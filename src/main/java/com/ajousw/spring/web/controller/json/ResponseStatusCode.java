package com.ajousw.spring.web.controller.json;

public final class ResponseStatusCode {
    public static final int OK = 200;
    public static final int FORBIDDEN = 403;
    public static final int URL_NOT_FOUND = 404;
    public static final int EMAIL_NOT_VERIFIED = 410;
    public static final int WRONG_PARAMETER = 420;
    public static final int LOGIN_FAILED = 430;
    public static final int SERVER_ERROR = 500;

    public static final int TOKEN_EXPIRED = 4011;
    public static final int TOKEN_IS_BLACKLIST = 4012;
    public static final int TOKEN_WRONG_SIGNATURE = 4013;
    public static final int TOKEN_HASH_NOT_SUPPORTED = 4014;
    public static final int NO_AUTH_HEADER = 4015;
    public static final int TOKEN_VALIDATION_TRY_FAILED = 4016;

    private ResponseStatusCode() {
    }
}

