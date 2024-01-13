package com.ajousw.spring.domain.auth.config;

import com.ajousw.spring.domain.auth.jwt.JwtAccessDeniedHandler;
import com.ajousw.spring.domain.auth.jwt.JwtAuthenticationEntryPoint;
import com.ajousw.spring.domain.auth.jwt.JwtProperties;
import com.ajousw.spring.domain.auth.jwt.redis.RedisAccessTokenBlackListRepository;
import com.ajousw.spring.domain.auth.jwt.token.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private final RedisAccessTokenBlackListRepository blackListRepository;

    @Bean
    public TokenProvider tokenProvider(JwtProperties jwtProperties) {
        return new TokenProvider(jwtProperties.getSecret(), blackListRepository);
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    @Bean
    public JwtAccessDeniedHandler jwtAccessDeniedHandler() {
        return new JwtAccessDeniedHandler();
    }
}
