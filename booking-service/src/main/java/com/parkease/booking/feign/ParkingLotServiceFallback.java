package com.parkease.booking.feign;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Collections;

@Component
public class ParkingLotServiceFallback implements ParkingLotServiceClient {
    public void decrementAvailable(Long lotId) { }
    public void incrementAvailable(Long lotId) { }
    
    public Map<String, Object> getLot(Long lotId) {
        return Collections.emptyMap();
    }
}
