package com.parkease.analytics.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record LotReport(
        Long lotId,
        LocalDate date,
        long logCount,
        double occupancyRate,
        Map<Integer, Double> occupancyByHour,
        List<Integer> peakHours,
        BigDecimal revenue,
        Map<LocalDate, BigDecimal> revenueByDay,
        Map<String, Long> mostUsedSpotTypes,
        double averageDurationMinutes,
        LocalDateTime generatedAt
) {
}
