package com.parkease.analytics.service;

import com.parkease.analytics.entity.OccupancyLog;
import com.parkease.analytics.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventListener {
    
    private final AnalyticsRepository analyticsRepository;
    
    @RabbitListener(queues = "booking.created")
    public void handleBookingCreated(Map<String, Object> event) {
        try {
            Long lotId = toLong(event.get("lotId"));
            Long spotId = toLong(event.get("spotId"));
            
            OccupancyLog occupancyLog = OccupancyLog.builder()
                    .lotId(lotId)
                    .spotId(spotId)
                    .eventType("BOOKING_CREATED")
                    .timestamp(LocalDateTime.now())
                    .occupancyRate(0.0)
                    .availableSpots(0)
                    .totalSpots(0)
                    .build();
            
            analyticsRepository.save(occupancyLog);
            log.info("Recorded booking created analytics for lot: {}, spot: {}", lotId, spotId);
            
        } catch (Exception e) {
            log.error("Failed to record booking created analytics", e);
        }
    }
    
    @RabbitListener(queues = "booking.checkin")
    public void handleCheckIn(Map<String, Object> event) {
        try {
            Long lotId = toLong(event.get("lotId"));
            Long spotId = toLong(event.get("spotId"));
            
            OccupancyLog occupancyLog = OccupancyLog.builder()
                    .lotId(lotId)
                    .spotId(spotId)
                    .eventType("CHECK_IN")
                    .timestamp(LocalDateTime.now())
                    .occupancyRate(0.0)
                    .availableSpots(0)
                    .totalSpots(0)
                    .build();
            
            analyticsRepository.save(occupancyLog);
            log.info("Recorded check-in analytics for lot: {}, spot: {}", lotId, spotId);
            
        } catch (Exception e) {
            log.error("Failed to record check-in analytics", e);
        }
    }
    
    @RabbitListener(queues = "booking.checkout")
    public void handleCheckOut(Map<String, Object> event) {
        try {
            Long lotId = toLong(event.get("lotId"));
            Long spotId = toLong(event.get("spotId"));
            BigDecimal amount = toBigDecimal(event.get("amount"));
            
            OccupancyLog occupancyLog = OccupancyLog.builder()
                    .lotId(lotId)
                    .spotId(spotId)
                    .eventType("CHECK_OUT")
                    .revenue(amount)
                    .timestamp(LocalDateTime.now())
                    .occupancyRate(0.0)
                    .availableSpots(0)
                    .totalSpots(0)
                    .build();
            
            analyticsRepository.save(occupancyLog);
            log.info("Recorded check-out analytics for lot: {}, spot: {}, revenue: {}", lotId, spotId, amount);
            
        } catch (Exception e) {
            log.error("Failed to record check-out analytics", e);
        }
    }
    
    @RabbitListener(queues = "payment.completed")
    public void handlePaymentCompleted(Map<String, Object> event) {
        try {
            Long lotId = toLong(event.get("lotId"));
            BigDecimal amount = toBigDecimal(event.get("amount"));
            
            OccupancyLog occupancyLog = OccupancyLog.builder()
                    .lotId(lotId)
                    .eventType("PAYMENT_COMPLETED")
                    .revenue(amount)
                    .timestamp(LocalDateTime.now())
                    .occupancyRate(0.0)
                    .availableSpots(0)
                    .totalSpots(0)
                    .build();
            
            analyticsRepository.save(occupancyLog);
            log.info("Recorded payment analytics for lot: {}, amount: {}", lotId, amount);
            
        } catch (Exception e) {
            log.error("Failed to record payment analytics", e);
        }
    }
    
    private Long toLong(Object value) {
        if (value instanceof Number number) return number.longValue();
        if (value == null) return 0L;
        return Long.parseLong(String.valueOf(value));
    }
    
    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        if (value == null) return BigDecimal.ZERO;
        return new BigDecimal(String.valueOf(value));
    }
}