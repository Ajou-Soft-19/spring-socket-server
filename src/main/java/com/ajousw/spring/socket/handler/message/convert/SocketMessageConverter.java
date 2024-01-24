package com.ajousw.spring.socket.handler.message.convert;

import com.ajousw.spring.socket.handler.message.SocketRequest;
import com.ajousw.spring.socket.handler.message.SocketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocketMessageConverter {
    private final ObjectMapper objectMapper;

    public SocketRequest checkRequestValidity(WebSocketSession session, WebSocketMessage<?> message)
            throws IOException {
        if (!(message instanceof TextMessage textMessage)) {
            sendTextMessage(session, "Only Text JSON Message is Allowed", 420);
            return null;
        }

        SocketRequest socketRequest = convertFromJson(textMessage.getPayload(),
                SocketRequest.class);
        if (socketRequest == null) {
            log.info("<{}> wrong request type", session.getId());
            sendTextMessage(session, "Error while parsing JSON. Check Request JSON Form", 420);
            return null;
        }

        if (checkJwtToken(session, socketRequest.getJwt())) {
            sendTextMessage(session,
                    "Authentication Error: Please use the token used during Handshake.", 420);
            return null;
        }
        return socketRequest;
    }

    private boolean checkJwtToken(WebSocketSession session, String jwt) throws IOException {
        return !Objects.equals(session.getHandshakeHeaders().getFirst("Authorization"), jwt);
    }

    public <T> T convertFromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.info("Error while parsing JSON to object");
            return null;
        }
    }

    public String convertToJson(Object clazz) {
        try {
            return objectMapper.writeValueAsString(clazz);
        } catch (IOException e) {
            log.error("Error while writing JSON to object: {}", clazz, e);
            return "Error while parsing JSON to object";
        }
    }

    public void sendTextMessage(WebSocketSession session, String text, int code) throws IOException {
        String responseJson = convertToJson(Map.of("msg", new SocketResponse(code, text)));
        session.sendMessage(new TextMessage(responseJson));
    }

    public void sendObjectMessage(WebSocketSession session, Object message, int code) throws IOException {
        String responseJson = convertToJson(Map.of("data", new SocketResponse(code, message)));
        session.sendMessage(new TextMessage(responseJson));
    }
}
