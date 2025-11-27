package com.jung.creatorlink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 서버라서)
                .authorizeHttpRequests(auth -> auth
                        // 나중에 인증 붙이면 여기서 경로별 권한 나눌 예정
                        .anyRequest().permitAll() // 지금은 모든 요청 허용
                );

        return http.build();
    }
}