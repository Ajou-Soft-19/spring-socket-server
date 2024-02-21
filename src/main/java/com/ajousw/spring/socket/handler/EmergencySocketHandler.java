package com.ajousw.spring.socket.handler;

import com.ajousw.spring.socket.handler.message.SocketRequest;
import com.ajousw.spring.socket.handler.message.SocketResponse;
import com.ajousw.spring.socket.handler.message.convert.SocketMessageConverter;
import com.ajousw.spring.socket.handler.service.EmergencySocketService;
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
public class EmergencySocketHandler implements WebSocketHandler {

    private final EmergencySocketService socketService;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final SocketMessageConverter socketMessageConverter;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("<{}> Emergency Vehicle Connected", session.getId());
        log.info("<{}> email : {}", session.getId(), session.getAttributes().get("email"));
        sessions.add(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        SocketRequest socketRequest = socketMessageConverter.checkRequestValidity(session, message);
        if (socketRequest == null) {
            return;
        }

        long startTime = System.currentTimeMillis();
        SocketResponse socketResponse = socketService.handleSocketRequest(socketRequest, session);
        long endTime = System.currentTimeMillis();

        log.info("<{}> Response Time = {}ms", session.getId().substring(0, 13), endTime - startTime);
        if (!session.isOpen()) {
            log.info("Session Closed while sending data: " + session.getId());
            return;
        }

        session.sendMessage(new TextMessage(socketMessageConverter.convertToJson(socketResponse)));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("[Session Manager] Error occurred at sender " + session);
        socketService.deleteStatus(session.getAttributes());
        sessions.remove(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.info("[Session Manager] Session " + session.getId() + " closed with status: " + closeStatus.getReason());
        socketService.deleteStatus(session.getAttributes());
        sessions.remove(session);
    }

    public void disconnectTargetSessions(Set<String> targetSessionId) {
        List<WebSocketSession> targetSessions = sessions.stream()
                .filter(s -> targetSessionId.contains(s.getId()))
                .toList();

        log.info("[Session Manager] Disconnecting Expired Emergency Vehicles : {}", targetSessions.size());
        for (WebSocketSession session : targetSessions) {
            try {
                if (session.isOpen()) {
                    session.close();
                    log.info("[Session Manager] Closed session: {}", session.getId());
                }
            } catch (IOException e) {
                log.error("[Session Manager] Failed to close session {}", session.getId(), e);
            }
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
