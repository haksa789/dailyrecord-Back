package com.dailyrecord.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class SecurityConfig {

    private final JwtCookieFilter jwtCookieFilter; // 새로 추가된 필터

    public SecurityConfig(JwtCookieFilter jwtCookieFilter) {
        this.jwtCookieFilter = jwtCookieFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // CSRF 비활성화
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안 함
                .and()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/members/register", "/api/members/login").permitAll() // 로그인/회원가입은 인증 필요 없음
                        .requestMatchers(HttpMethod.GET, "/api/members/me").authenticated() // 인증 필요
                        .anyRequest().permitAll() // 나머지 요청 허용
                )
                .cors().configurationSource(corsConfigurationSource()) // CORS 설정 추가
                .and()
                .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class); // JWT 쿠키 필터 추가

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); // React 클라이언트 URL
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(Collections.singletonList("*")); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 쿠키 포함 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
