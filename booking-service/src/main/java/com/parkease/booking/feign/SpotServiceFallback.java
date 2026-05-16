package com.parkease.booking.feign;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SpotServiceFallback implements SpotServiceClient {
    public Map<String, Object> getSpot(Long spotId) { return Map.of("error", "spot-service unavailable"); }
    public java.util.List<Map<String, Object>> getAvailableSpots(Long lotId) { return java.util.List.of(); }
    public Map<String, Object> reserveSpot(Long spotId) { return Map.of("error", "spot-service unavailable"); }
    public Map<String, Object> occupySpot(Long spotId) { return Map.of("error", "spot-service unavailable"); }
    public Map<String, Object> releaseSpot(Long spotId) { return Map.of("error", "spot-service unavailable"); }
}
