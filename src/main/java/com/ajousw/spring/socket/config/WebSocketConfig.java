package com.ajousw.spring.socket.config;

import com.ajousw.spring.socket.handler.EmergencySocketHandler;
import com.ajousw.spring.socket.handler.HandshakeInterceptor;
import com.ajousw.spring.socket.handler.LocationSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final HandshakeInterceptor handshakeInterceptor;
    private final LocationSocketHandler locationSocketHandler;
    private final EmergencySocketHandler emergencySocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(locationSocketHandler, "/ws/my-location")
                .addHandler(emergencySocketHandler, "/ws/emergency-location")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*");
    }

}
