package com.ajousw.spring.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 로그 ID를 남기기 위한 로깅 필터 필터 최상단에 존재한다. IP 출력을 위해 -Djava.net.preferIPv4Stack=true 옵션 설정
 */
@Slf4j
@Component
@Order(SecurityProperties.DEFAULT_FILTER_ORDER - 2)
public class LogFilter extends OncePerRequestFilter {
    public static final String TRACE_ID = "traceId";
    public static final String[] noFilterUrl = {"/error", "/favicon.ico"};
    private static final String X_FORWARD = "X-FORWARDED-FOR";
    private static final String LOG_START_FORMAT = "[REQUEST URI : {}, METHOD : {}, IP : {}]";
    private static final String LOG_END_FORMAT = "Response Time = {}ms";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String uuid = UUID.randomUUID().toString().substring(24, 36);

        if (isNoLoggingUrl(requestURI)) {
            filterChain.doFilter(request, response);
            MDC.clear();
            return;
        }

        String ip = getRequestIp(request);
        long startTime = startLogging(request, requestURI, uuid, ip);

        filterChain.doFilter(request, response);

        endLogging(startTime);
    }

    private boolean isNoLoggingUrl(String requestURI) {
        return PatternMatchUtils.simpleMatch(noFilterUrl, requestURI);
    }

    private String getRequestIp(HttpServletRequest request) {
        String ip = request.getHeader(X_FORWARD);
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private long startLogging(HttpServletRequest request, String requestURI, String uuid, String ip) {
        MDC.put(TRACE_ID, uuid);
        long startTime = System.currentTimeMillis();
        log.info(LOG_START_FORMAT, requestURI, request.getMethod(), ip);
        return startTime;
    }

    private void endLogging(long startTime) {
        long totalTime = System.currentTimeMillis() - startTime;
        log.info(LOG_END_FORMAT, totalTime);
        MDC.clear();
    }
}

