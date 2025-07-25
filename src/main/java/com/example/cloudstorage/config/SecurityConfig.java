package com.example.cloudstorage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // отключаем CSRF для POST
                .cors(cors -> {}) // разрешаем CORS
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/logout").permitAll() // логин и логаут открыты
                        .anyRequest().authenticated() // остальное требует авторизации
                );

        return http.build();
    }
}
