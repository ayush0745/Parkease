package com.parkease.payment.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig — stateless JWT-based security for payment-service.
 *
 * In the ParkEase gateway pattern, the API Gateway validates the JWT and
 * forwards the authenticated user ID as the X-User-Id header. This service
 * trusts that header — it does NOT re-validate the JWT itself.
 *
 * Public endpoints: /actuator/health, /health, /swagger-ui/**, /v3/api-docs/**
 * Endpoint-level ownership/admin checks are enforced in PaymentController
 * using the X-User-Id and X-User-Role headers forwarded by the gateway.
 *
 * FIX: security package was completely empty in original code.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — stateless REST API, no session cookies
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless — no HttpSession
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Public — health checks and API docs
                .requestMatchers(
                    "/api/v1/payments/health",
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // Gateway validates JWTs; controllers enforce required forwarded headers.
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
