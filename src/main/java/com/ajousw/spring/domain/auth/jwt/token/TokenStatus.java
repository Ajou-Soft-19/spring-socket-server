package com.ajousw.spring.domain.auth.jwt.token;

public enum TokenStatus {
    TOKEN_VALID("Valid Token", "정상 토큰입니다."),
    TOKEN_EXPIRED("%s Expired", "%s이 만료되었습니다."),
    TOKEN_IS_BLACKLIST("%s Discarded", "%s이 폐기된 상태입니다."),
    TOKEN_WRONG_SIGNATURE("Wrong %s", "잘못된 %s입니다."),
    TOKEN_HASH_NOT_SUPPORTED("%s Unsupported", "지원하지 않는 형식의 %s입니다."),
    WRONG_AUTH_HEADER("Wrong Authorization Header", "[Bearer ]로 시작하는 %s이 없습니다."),
    TOKEN_ID_NOT_MATCH("Token ID Not Match", "TokenId가 서로 일치하지 않습니다."),
    TOKEN_VALIDATION_TRY_FAILED("Wrong Authentication", "인증에 실패했습니다.");

    private final String messageEn;
    private final String messageKr;


    TokenStatus(String messageEn, String messageKr) {
        this.messageEn = messageEn;
        this.messageKr = messageKr;
    }

    public String getMessageEn(TokenType tokenType) {
        if (tokenType == null) {
            return String.format(messageEn, "Token");
        }

        if (tokenType == TokenType.ACCESS) {
            return String.format(messageEn, "Access Token");
        }

        return String.format(messageEn, "Refresh Token");
    }

    public String getMessageKr(TokenType tokenType) {
        if (tokenType == null) {
            return String.format(messageKr, "Token");
        }

        if (tokenType == TokenType.ACCESS) {
            return String.format(messageKr, "Access Token");
        }

        return String.format(messageKr, "Refresh Token");
    }
}
