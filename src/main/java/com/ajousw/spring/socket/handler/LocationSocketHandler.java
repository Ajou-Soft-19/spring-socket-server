package com.ajousw.spring.socket.handler;

import com.ajousw.spring.socket.MemberSocketController;
import com.ajousw.spring.socket.handler.message.MessageType;
import com.ajousw.spring.socket.handler.message.SocketRequest;
import com.ajousw.spring.socket.handler.message.SocketResponse;
import com.ajousw.spring.socket.handler.message.convert.SocketMessageConverter;
import java.io.IOException;
import java.util.List;
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

    private final MemberSocketController memberSocketController;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final SocketMessageConverter socketMessageConverter;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("<{}> Connected", session.getId());
        boolean isGuest = (boolean) session.getAttributes().get("isGuest");
        if (isGuest) {
            log.info("<{}> guest session", session.getId());
        } else {
            log.info("<{}> email : {}", session.getId(), session.getAttributes().get("email"));
        }

        sessions.add(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        SocketRequest socketRequest = socketMessageConverter.checkRequestValidity(session, message);
        if (socketRequest == null) {
            return;
        }

        long startTime = System.currentTimeMillis();
        SocketResponse socketResponse = memberSocketController.handleSocketRequest(socketRequest, session);
        long endTime = System.currentTimeMillis();
        log.info("<{}> Response Time = {}ms", session.getId(), endTime - startTime);
        if (!session.isOpen()) {
            log.info("Session Closed while sending data: " + session.getId());
            return;
        }

        session.sendMessage(new TextMessage(socketMessageConverter.convertToJson(socketResponse)));
    }


    public void broadcastToTargetSession(Set<String> targetSessionId, Object message, MessageType messageType) {
        List<WebSocketSession> targetSessions = sessions.stream()
                .filter(s -> targetSessionId.contains(s.getId()))
                .toList();

        for (WebSocketSession session : targetSessions) {
            try {
                if (session.isOpen()) {
                    socketMessageConverter.sendObjectMessage(session, message, messageType);
                }
            } catch (IOException e) {
                log.error("Failed to send message to {}", session.getId(), e);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.info("Error occurred at sender " + session);
        memberSocketController.deleteStatus(session.getAttributes());
        sessions.remove(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.info("Session " + session.getId() + " closed with status: " + closeStatus.getReason());
        memberSocketController.deleteStatus(session.getAttributes());
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
