package com.parkease.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private Long totalUsers;
    private Long totalDrivers;
    private Long totalManagers;
    private Long totalLots;
    private Long approvedLots;
    private Long pendingLots;
    private Long totalSpots;
    private Long availableSpots;
    private Long totalBookings;
    private Long activeBookings;
    private Long completedBookings;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal monthlyRevenue;
    private Double averageOccupancy;
    private Map<String, Long> bookingsByStatus;
    private Map<String, BigDecimal> revenueByMonth;
    private Map<String, Long> usersByRole;
    private LocalDateTime lastUpdated;
}