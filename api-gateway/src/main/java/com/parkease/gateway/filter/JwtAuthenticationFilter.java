package com.parkease.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;


@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            
            // Skip auth for public endpoints
            String method = exchange.getRequest().getMethod() == null
                    ? "GET"
                    : exchange.getRequest().getMethod().name();

            if (isPublicPath(method, path)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);
            
            try {
                SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
                JwtParser parser = Jwts.parser().verifyWith(key).build();
                Jws<Claims> claimsJws = parser.parseSignedClaims(token);
                Claims claims = claimsJws.getPayload();
                String role = claims.get("role", String.class);

                if (!isAuthorized(method, path, role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                
                ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(claims.get("userId")))
                    .header("X-User-Role", role)
                    .build();
                
                return chain.filter(exchange.mutate().request(request).build());
            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private boolean isPublicPath(String method, String path) {
        // Always allow OPTIONS preflight requests
        if (HttpMethod.OPTIONS.matches(method)) return true;

        // Always allow public auth endpoints
        if (path.equals("/api/v1/auth/register") ||
            path.equals("/api/v1/auth/login") ||
            path.equals("/api/v1/auth/logout") ||
            path.equals("/api/v1/auth/refresh") ||
            path.equals("/api/v1/auth/forgot-password") ||
            path.equals("/api/v1/auth/reset-password") ||
            path.startsWith("/api/v1/auth/oauth") ||
            path.startsWith("/actuator") ||
            path.endsWith("/health")) {
            return true;
        }

        // Public GET for lots and spots
        if (HttpMethod.GET.matches(method) &&
            (path.startsWith("/api/v1/lots") || path.startsWith("/api/v1/spots"))) {
            return true;
        }

        return false;
    }

    private boolean isAuthorized(String method, String path, String role) {
        if ("ADMIN".equals(role)) {
            return true;
        }
        if (path.startsWith("/api/v1/auth/admin") || path.contains("/approve") || path.contains("/reject")) {
            return false;
        }
        if (path.startsWith("/api/v1/lots") || path.startsWith("/api/v1/spots") || path.startsWith("/api/v1/analytics")) {
            return "MANAGER".equals(role);
        }
        if (path.startsWith("/api/v1/notifications/bulk")) {
            return "MANAGER".equals(role);
        }
        return "DRIVER".equals(role) || "MANAGER".equals(role);
    }

    public static class Config {
    }
}
