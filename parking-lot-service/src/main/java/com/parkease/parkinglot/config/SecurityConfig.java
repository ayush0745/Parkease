package com.parkease.parkinglot.config;

import com.parkease.parkinglot.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public: health, Swagger, guest browsing
                .requestMatchers(
                    "/api/v1/lots/health",
                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                    "/actuator/**"
                ).permitAll()
                // Guests can search/browse without login
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/lots/city/**",
                    "/api/v1/lots/nearby",
                    "/api/v1/lots/search",
                    "/api/v1/lots/available",
                    "/api/v1/lots/{lotId}"
                ).permitAll()
                // Admin-only endpoints
                .requestMatchers("/api/v1/lots/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/lots/*/approve").hasRole("ADMIN")
                .requestMatchers("/api/v1/lots/*/reject").hasRole("ADMIN")
                // Internal inter-service calls
                .requestMatchers("/api/v1/lots/*/availability/**").authenticated()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
