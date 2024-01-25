package com.ajousw.spring.socket.handler.pubsub;

import com.ajousw.spring.socket.handler.LocationSocketHandler;
import com.ajousw.spring.socket.handler.message.dto.BroadcastDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertBroadcastListener implements MessageListener {
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;
    private LocationSocketHandler locationSocketHandler = null;

    public AlertBroadcastListener(ObjectMapper objectMapper, ApplicationContext applicationContext) {
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            BroadcastDto broadcastDto = objectMapper.readValue(message.getBody(), BroadcastDto.class);
            getLocationSocketHandler().broadcastToTargetSession(broadcastDto.getTargetSession(),
                    broadcastDto.getData());
            log.info("broadcast alert message to {}", broadcastDto.getTargetSession());
        } catch (IOException e) {
            log.error("error while listening broadcast message");
            throw new IllegalStateException(e);
        }
    }

    private LocationSocketHandler getLocationSocketHandler() {
        if (locationSocketHandler == null) {
            locationSocketHandler = applicationContext.getBean(LocationSocketHandler.class);
        }

        return locationSocketHandler;
    }

}