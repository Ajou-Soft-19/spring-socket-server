package com.ajousw.spring.socket.config;


import com.ajousw.spring.domain.auth.jwt.token.TokenValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import java.util.Map;
import java.util.Objects;

import static com.ajousw.spring.domain.auth.jwt.token.TokenStatus.WRONG_AUTH_HEADER;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandshakeInterceptor implements org.springframework.web.socket.server.HandshakeInterceptor {

    private static final String VALIDATION_RESULT_KEY = "result";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if (!(request instanceof ServletServerHttpRequest)) {
            return false;
        }
        return setMemberInfoToAttributes((ServletServerHttpRequest) request, attributes);
    }

    private boolean setMemberInfoToAttributes(ServletServerHttpRequest request, Map<String, Object> attributes) {
        HttpServletRequest httpRequest = request.getServletRequest();
        TokenValidationResult result = (TokenValidationResult) httpRequest.getAttribute(VALIDATION_RESULT_KEY);
        log.info("1");

        if (Objects.equals(httpRequest.getRequestURI(), "/ws/my-location")) {
            if (result.getTokenStatus() == WRONG_AUTH_HEADER) {
                setGuestInfo(attributes);
                return true;
            }

            if (!result.isValid()) {
                return false;
            }

            setLoginUserInfo(httpRequest, attributes, false);
            return true;
        }

        if (Objects.equals(httpRequest.getRequestURI(), "/ws/emergency-location")) {
            log.info("1");
            return setEmergencyLoginInfo(attributes, httpRequest, result);
        }

        return false;
    }

    private static void setGuestInfo(Map<String, Object> attributes) {
        attributes.put("isGuest", true);
        attributes.put("isEmergency", false);
    }

    private boolean setEmergencyLoginInfo(Map<String, Object> attributes, HttpServletRequest httpRequest, TokenValidationResult result) {
        log.info("{}", result);
        if (!result.isValid()) {
            return false;
        }

        setLoginUserInfo(httpRequest, attributes, true);
        return true;
    }

    private void setLoginUserInfo(HttpServletRequest httpRequest, Map<String, Object> attributes, boolean isEmergency) {
        String email = (String) httpRequest.getAttribute("email");
        String role = (String) httpRequest.getAttribute("role");
        String username = (String) httpRequest.getAttribute("username");
        String tokenId = (String) httpRequest.getAttribute("tokenId");

        attributes.put("isGuest", false);
        attributes.put("isEmergency", isEmergency);
        attributes.put("email", email);
        attributes.put("role", role);
        attributes.put("username", username);
        attributes.put("tokenId", tokenId);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {

    }

}
