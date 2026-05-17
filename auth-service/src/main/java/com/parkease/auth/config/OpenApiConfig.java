package com.parkease.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI authOpenApi() {
        return new OpenAPI()
                .info(new Info().title("ParkEase Auth API").version("1.0.0").description("JWT, OAuth2 hooks, profile and admin user management"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .components(new Components().addSecuritySchemes("Bearer Token",
                        new SecurityScheme()
                                .name("Bearer Token")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
