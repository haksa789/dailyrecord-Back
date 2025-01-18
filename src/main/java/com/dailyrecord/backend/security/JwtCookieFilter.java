package com.dailyrecord.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class JwtCookieFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtCookieFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 쿠키에서 JWT 토큰 가져오기
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) { // 쿠키 이름이 "authToken"인지 확인
                    String token = cookie.getValue();

                    // 토큰 유효성 확인 및 사용자 인증
                    if (jwtTokenProvider.validateToken(token)) {
                        SecurityContextHolder.getContext().setAuthentication(jwtTokenProvider.getAuthentication(token));
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
