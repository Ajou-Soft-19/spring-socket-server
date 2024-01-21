package com.ajousw.spring.socket;


import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ChannelTopic alertBroadcast;
    private final ObjectMapper objectMapper;

//    public void publishAlertMessageToSocket(BroadcastDto broadcastDto) {
//        try {
//            redisTemplate.convertAndSend(alertBroadcast.getTopic(), objectMapper.writeValueAsString(broadcastDto));
//        } catch (IOException e) {
//            log.error("error while publishing broadcastDto message {}", broadcastDto, e);
//        }
//    }

}
