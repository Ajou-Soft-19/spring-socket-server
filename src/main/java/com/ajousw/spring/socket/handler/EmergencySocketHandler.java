package com.ajousw.spring.socket.handler;

import com.ajousw.spring.socket.EmergencySocketController;
import com.ajousw.spring.socket.handler.message.SocketRequest;
import com.ajousw.spring.socket.handler.message.SocketResponse;
import com.ajousw.spring.socket.handler.message.convert.SocketMessageConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmergencySocketHandler implements WebSocketHandler {

    private final EmergencySocketController socketController;
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
        SocketResponse socketResponse = socketController.handleSocketRequest(socketRequest, session);
        long endTime = System.currentTimeMillis();

        log.info("<{}> Response Time = {}ms", session.getId(), endTime - startTime);
        if (!session.isOpen()) {
            log.info("Session Closed while sending data: " + session.getId());
            return;
        }

        session.sendMessage(new TextMessage(socketMessageConverter.convertToJson(socketResponse)));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Error occurred at sender " + session);
        socketController.deleteStatus(session.getAttributes());
        sessions.remove(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.info("Session " + session.getId() + " closed with status: " + closeStatus.getReason());
        socketController.deleteStatus(session.getAttributes());
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
