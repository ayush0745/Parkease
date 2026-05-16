package com.parkease.booking.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Map;

@FeignClient(name = "parking-lot-service", fallback = ParkingLotServiceFallback.class)
public interface ParkingLotServiceClient {
    @PostMapping("/api/v1/lots/{lotId}/availability/decrement")
    void decrementAvailable(@PathVariable Long lotId);

    @PostMapping("/api/v1/lots/{lotId}/availability/increment")
    void incrementAvailable(@PathVariable Long lotId);

    @GetMapping("/api/v1/lots/{lotId}")
    Map<String, Object> getLot(@PathVariable Long lotId);
}
