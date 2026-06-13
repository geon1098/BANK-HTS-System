package com.bank.security;

import com.bank.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-ms}") long expirationMs) {
        // 비밀키 문자열로 서명용 키 생성 (HS256)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** 로그인 성공 시 토큰 발급 */
    public String createToken(String username, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)               // 누구인지
                .claim("role", role.name())      // 권한
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)                   // 서명(도장)
                .compact();
    }

    /** 토큰에서 username 꺼내기 */
    public String getUsername(String token) {
        return parse(token).getPayload().getSubject();
    }

    /** 토큰이 유효한가? (서명/만료 검증) */
    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;   // 위조됐거나 만료됨
        }
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}