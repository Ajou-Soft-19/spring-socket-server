package com.ajousw.spring.socket.handler;

import com.ajousw.spring.socket.SocketController;
import com.ajousw.spring.socket.handler.json.SocketRequest;
import com.ajousw.spring.socket.handler.json.SocketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationSocketHandler implements WebSocketHandler {

    private final SocketController socketController;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("<{}> Connected", session.getId());
        log.info("<{}> email : {}", session.getId(), session.getAttributes().get("email"));
        sessions.add(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        if (!(message instanceof TextMessage textMessage)) {
            sendTextMessage(session, "Only Text JSON Message is Allowed");
            return;
        }

        SocketRequest socketRequest = convertFromJson(textMessage.getPayload(), SocketRequest.class);
        if (socketRequest == null) {
            log.info("<{}> wrong request type", session.getId());
            sendTextMessage(session, "Error while parsing JSON. Check Request JSON Form");
            return;
        }

        if (checkJwtToken(session, socketRequest.getJwt())) {
            sendTextMessage(session, "Authentication Error: Please use the token used during Handshake.");
            return;
        }

        long startTime = System.currentTimeMillis();
        SocketResponse socketResponse = socketController.handleSocketRequest(socketRequest, session);
        long endTime = System.currentTimeMillis();
        log.info("<{}> Response Time = {}ms", session.getId(), endTime - startTime);
        session.sendMessage(new TextMessage(convertToJson(socketResponse)));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Error occurred at sender " + session, exception);
        // TODO: 오류 시 세션 삭제
        sessions.remove(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.info("Session " + session.getId() + " closed with status: " + closeStatus.getReason());
        sessions.remove(session);
    }

    public void broadcastToTargetSession(Set<String> targetSessionId, String message) {
        TextMessage textMessage = new TextMessage(message);
        List<WebSocketSession> targetSessions = sessions.stream()
                .filter(s -> targetSessionId.contains(s.getId()))
                .toList();

        for (WebSocketSession session : targetSessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("Failed to send message to {}", session.getId(), e);
            }
        }
    }

    private void sendTextMessage(WebSocketSession session, String text) throws IOException {
        String errorResponse = convertToJson(new SocketResponse(text));
        session.sendMessage(new TextMessage(errorResponse));
    }

    private boolean checkJwtToken(WebSocketSession session, String jwt) throws IOException {
        return !Objects.equals(session.getHandshakeHeaders().getFirst("Authorization"), jwt);
    }

    private <T> T convertFromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.info("Error while parsing JSON to object");
            return null;
        }
    }

    private String convertToJson(Object clazz) {
        try {
            return objectMapper.writeValueAsString(clazz);
        } catch (IOException e) {
            log.error("Error while writing JSON to object: {}", clazz, e);
            return "Error while parsing JSON to object";
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
