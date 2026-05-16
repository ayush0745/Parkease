package com.parkease.parkinglot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI serviceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ParkEase parking-lot-service API")
                .version("1.0.0")
                .description("ParkEase Smart Parking Management Platform - parking-lot-service"));
    }
}
