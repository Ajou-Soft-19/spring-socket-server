package com.ajousw.spring.domain.auth.jwt.token;

import com.ajousw.spring.domain.auth.jwt.redis.RedisAccessTokenBlackListRepository;
import com.ajousw.spring.domain.member.UserPrinciple;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Slf4j
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String TOKEN_ID_KEY = "tokenId";
    private static final String USERNAME_KEY = "username";

    protected final String secrete;
    protected final Key hashKey;

    protected final RedisAccessTokenBlackListRepository blackListRepository;

    public TokenProvider(String secrete, RedisAccessTokenBlackListRepository blackListRepository) {
        this.secrete = secrete;
        byte[] keyBytes = Decoders.BASE64.decode(secrete);
        this.hashKey = Keys.hmacShaKeyFor(keyBytes);
        this.blackListRepository = blackListRepository;
    }

    // 토큰 유효성 검사 -> access, refresh 토큰 둘 다 검증하는 함수이므로 access 토큰 블랙리스트는 체크하지 않는다.
    public TokenValidationResult validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(hashKey).build().parseClaimsJws(token).getBody();
            TokenType tokenType = claims.get(AUTHORITIES_KEY) == null ? TokenType.REFRESH : TokenType.ACCESS;
            return new TokenValidationResult(TokenStatus.TOKEN_VALID, tokenType, claims.get(TOKEN_ID_KEY, String.class),
                    claims);
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰");
            return getExpiredTokenValidationResult(e);
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명");
            return new TokenValidationResult(TokenStatus.TOKEN_WRONG_SIGNATURE, null, null, null);
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 서명");
            return new TokenValidationResult(TokenStatus.TOKEN_HASH_NOT_SUPPORTED, null, null, null);
        } catch (IllegalArgumentException e) {
            log.info("잘못된 JWT 토큰");
            return new TokenValidationResult(TokenStatus.TOKEN_WRONG_SIGNATURE, null, null, null);
        }
    }

    private TokenValidationResult getExpiredTokenValidationResult(ExpiredJwtException e) {
        Claims claims = e.getClaims();
        TokenType tokenType = claims.get(AUTHORITIES_KEY) == null ? TokenType.REFRESH : TokenType.ACCESS;
        return new TokenValidationResult(TokenStatus.TOKEN_EXPIRED, tokenType, claims.get(TOKEN_ID_KEY, String.class),
                null);
    }

    // access 토큰을 인자로 전달받아 클레임을 만들어 권한 정보 반환
    public Authentication getAuthentication(String token, Claims claims) {
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // 커스텀한 UserPrinciple 객체 사용 -> 이후 추가적인 데이터를 토큰에 넣을 경우 UserPrinciple 객체 및 이 클래스의 함수들 수정 필요
        UserPrinciple principle = new UserPrinciple(claims.getSubject(), claims.get(USERNAME_KEY, String.class),
                authorities);

        return new UsernamePasswordAuthenticationToken(principle, token, authorities);
    }

    public boolean isAccessTokenBlackList(String accessToken) {
        if (blackListRepository.isKeyBlackList(accessToken)) {
            log.info("Blacklisted Access Token");
            return true;
        } else {
            return false;
        }
    }
}
