package com.parkease.analytics.dto;

import com.parkease.analytics.entity.OccupancyLog;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsDTO {
    private Long logId;
    private Long lotId;
    private Long spotId;
    private LocalDateTime timestamp;
    private Double occupancyRate;
    private Integer availableSpots;
    private Integer totalSpots;
    private OccupancyLog.VehicleType vehicleType;
    private String spotType;
    private String eventType;
    private Long bookingId;
    private BigDecimal amount;
    private String currency;
    private Long durationMinutes;
}
