package com.ajousw.spring.domain.auth.jwt;

import com.ajousw.spring.domain.auth.jwt.token.TokenProvider;
import com.ajousw.spring.domain.auth.jwt.token.TokenStatus;
import com.ajousw.spring.domain.auth.jwt.token.TokenType;
import com.ajousw.spring.domain.auth.jwt.token.TokenValidationResult;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_REGEX = "Bearer ([a-zA-Z0-9_\\-\\+\\/=]+)\\.([a-zA-Z0-9_\\-\\+\\/=]+)\\.([a-zA-Z0-9_.\\-\\+\\/=]*)";
    private static final Pattern BEARER_PATTERN = Pattern.compile(BEARER_REGEX);
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        // JWT 토큰 예외 구분 처리를 위해 request에 tokenValidationResult를 담아 EntryPoint에 전달
        // Authorization 헤더가 없는 경우
        if (!StringUtils.hasText(token)) {
            handleMissingToken(request, response, filterChain);
            return;
        }

        TokenValidationResult tokenValidationResult = tokenProvider.validateToken(token);

        // 잘못된 토큰일 경우 (잘못된 토큰, Refresh token을 넣은 경우)
        if (!tokenValidationResult.isValid() || tokenValidationResult.getTokenType() != TokenType.ACCESS) {
            handleWrongToken(request, response, filterChain, tokenValidationResult);

            return;
        }

        // 토큰이 블랙리스트인 경우
        if (tokenProvider.isAccessTokenBlackList(token)) {
            handleBlackListedToken(request, response, filterChain);
            return;
        }

        // 정상적인 토큰인 경우 SecurityContext에 Authentication 설정
        handleValidToken(token, tokenValidationResult.getClaims());
        setHeaderForSocketAuthentication(request, tokenValidationResult);
        filterChain.doFilter(request, response);
    }

    // 소켓 통신용 jwt 정보 전달 TODO: 만료 시간 설정할지 결정
    private void setHeaderForSocketAuthentication(HttpServletRequest request,
                                                  TokenValidationResult tokenValidationResult) {
        String email = tokenValidationResult.getClaims().getSubject();
        String role = (String) tokenValidationResult.getClaims().get("auth");
        String username = (String) tokenValidationResult.getClaims().get("username");
        String tokenId = (String) tokenValidationResult.getClaims().get("tokenId");

        request.setAttribute("result", tokenValidationResult);
        request.setAttribute("role", role);
        request.setAttribute("email", email);
        request.setAttribute("username", username);
        request.setAttribute("tokenId", tokenId);
    }

    private void handleValidToken(String token, Claims claims) {
        Authentication authentication = tokenProvider.getAuthentication(token, claims);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("AUTH SUCCESS : {},", authentication.getName());
    }

    private void handleBlackListedToken(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws IOException, ServletException {
        request.setAttribute("result",
                new TokenValidationResult(TokenStatus.TOKEN_IS_BLACKLIST, null, null, null)
        );
        filterChain.doFilter(request, response);
    }

    private void handleWrongToken(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain,
                                  TokenValidationResult tokenValidationResult) throws IOException, ServletException {
        request.setAttribute("result", tokenValidationResult);
        filterChain.doFilter(request, response);
    }

    private void handleMissingToken(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        request.setAttribute("result",
                new TokenValidationResult(TokenStatus.WRONG_AUTH_HEADER, null, null, null)
        );
        filterChain.doFilter(request, response);
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith("Bearer ") && BEARER_PATTERN.matcher(bearerToken).matches()) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
