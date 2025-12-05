package com.sopds.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Swagger / OpenAPI — ВСЕ пути
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/api-docs/**",
                                "/api-docs",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // Статика
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // Публичные эндпоинты
                        .requestMatchers("/", "/web", "/search/**", "/book", "/author", "/series", "/genre", "/catalog").permitAll()
                        .requestMatchers("/opds/**").permitAll()
                        .requestMatchers("/read/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        // Админка — только для аутентифицированных
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> {});

        return http.build();
    }
}
