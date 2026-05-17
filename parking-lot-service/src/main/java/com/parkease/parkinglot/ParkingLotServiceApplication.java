package com.parkease.parkinglot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class ParkingLotServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkingLotServiceApplication.class, args);
    }
}
