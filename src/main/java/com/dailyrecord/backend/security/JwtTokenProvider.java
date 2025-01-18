package com.dailyrecord.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret.key}")
    private String secretKey;

    private final long TOKEN_EXPIRATION_MS = 86400000L; // 24시간

    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // 토큰 생성
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // 사용자 이름 설정
                .setIssuedAt(new Date()) // 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MS)) // 만료 시간
                .signWith(SignatureAlgorithm.HS512, secretKey) // 서명 알고리즘과 비밀키 설정
                .compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token); // 토큰 파싱 및 서명 검증
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWT validation error: " + e.getMessage());
            return false;
        }
    }

    // 토큰에서 사용자 이름 추출
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 토큰에서 인증 정보 추출
    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token); // 토큰에서 사용자 이름 추출
        UserDetails userDetails = userDetailsService.loadUserByUsername(username); // 사용자 정보 로드
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}