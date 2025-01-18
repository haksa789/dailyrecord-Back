package com.dailyrecord.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                        .requestMatchers(
                                "/api/members/register",
                                "/api/members/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "api-docs"
                        ).permitAll() // 인증 없이 접근 가능
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class); // JWT 쿠키 필터 추가

        return http.build();
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
