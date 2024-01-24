package com.ajousw.spring.socket.handler.pubsub;


import com.ajousw.spring.socket.handler.message.dto.CurrentPointUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessagePublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic updateCurrentPathPoint;
    private final ObjectMapper objectMapper;

    public void publicPointUpdateMessageToSocket(CurrentPointUpdateDto pointUpdateDto) {
        try {
            redisTemplate.convertAndSend(updateCurrentPathPoint.getTopic(),
                    objectMapper.writeValueAsString(pointUpdateDto));
        } catch (IOException e) {
            log.error("error while publishing pointUpdateDto message {}", pointUpdateDto, e);
        }
    }

}
