package com.ajousw.spring.socket.config;


import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandshakeInterceptor implements org.springframework.web.socket.server.HandshakeInterceptor {

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
        String email = (String) httpRequest.getAttribute("email");
        String role = (String) httpRequest.getAttribute("role");
        String username = (String) httpRequest.getAttribute("username");
        String tokenId = (String) httpRequest.getAttribute("tokenId");

        if (!StringUtils.hasText(role) || !StringUtils.hasText(role) || !StringUtils.hasText(username)
                || !StringUtils.hasText(tokenId)) {
            return false;
        }

        attributes.put("email", email);
        attributes.put("role", role);
        attributes.put("username", username);
        attributes.put("tokenId", tokenId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {

    }

}
