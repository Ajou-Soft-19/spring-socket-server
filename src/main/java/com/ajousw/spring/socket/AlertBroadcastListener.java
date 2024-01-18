package com.ajousw.spring.socket;

import com.ajousw.spring.socket.handler.LocationSocketHandler;
import com.ajousw.spring.web.controller.dto.BroadcastDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertBroadcastListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final LocationSocketHandler locationSocketHandler;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            BroadcastDto broadcastDto = objectMapper.readValue(message.getBody(), BroadcastDto.class);
            locationSocketHandler.broadcastToTargetSession(broadcastDto.getTargetSession(), broadcastDto.getData());
            log.info("broadcast alert message to {}", broadcastDto.getTargetSession());
        } catch (IOException e) {
            log.error("error while listening broadcast message");
            throw new IllegalStateException(e);
        }
    }
    
}
