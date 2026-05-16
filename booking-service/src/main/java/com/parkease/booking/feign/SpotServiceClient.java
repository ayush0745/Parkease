package com.parkease.booking.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "spot-service", fallback = SpotServiceFallback.class)
public interface SpotServiceClient {
    @GetMapping("/api/v1/spots/{spotId}")
    Map<String, Object> getSpot(@PathVariable Long spotId);

    @GetMapping("/api/v1/spots/lot/{lotId}/available")
    java.util.List<java.util.Map<String, Object>> getAvailableSpots(@PathVariable Long lotId);

    @PatchMapping("/api/v1/spots/{spotId}/reserve")
    Map<String, Object> reserveSpot(@PathVariable Long spotId);

    @PatchMapping("/api/v1/spots/{spotId}/occupy")
    Map<String, Object> occupySpot(@PathVariable Long spotId);

    @PatchMapping("/api/v1/spots/{spotId}/release")
    Map<String, Object> releaseSpot(@PathVariable Long spotId);
}
