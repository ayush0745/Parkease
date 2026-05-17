package com.parkease.analytics.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PlatformSummary(
        long totalLogs,
        long activeLots,
        double averageOccupancyRate,
        BigDecimal totalRevenue,
        double averageDurationMinutes,
        LocalDateTime generatedAt
) {
}
