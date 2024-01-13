package com.ajousw.spring.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 스프링 시큐리티 사용으로 인해 서블릿부터 올라오는 예외를 처리하는 필터 서블릿은 스프링 ExceptionAdvice 대상이 아니기 때문에 스프링 시큐리티 앞에 필터를 붙어준다.
 */
@Slf4j
@Component
@Order(SecurityProperties.DEFAULT_FILTER_ORDER - 1)
public class ExceptionFilter extends OncePerRequestFilter {
    private static final String INTERNAL_SERVER_ERROR = "Unexpected Server Error";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            doFilter(request, response, filterChain);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected Server Error");
        }
    }
}