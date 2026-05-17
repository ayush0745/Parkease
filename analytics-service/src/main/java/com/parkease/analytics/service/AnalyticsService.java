package com.parkease.analytics.service;

import com.parkease.analytics.dto.AdminDashboardDTO;
import com.parkease.analytics.dto.AnalyticsDTO;
import com.parkease.analytics.dto.LotReport;
import com.parkease.analytics.dto.PlatformSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AnalyticsService {
    AnalyticsDTO logOccupancy(AnalyticsDTO dto);
    void logOccupancy(Long lotId, Long spotId);
    Optional<AnalyticsDTO> getLog(Long logId);
    Page<AnalyticsDTO> getByLot(Long lotId, Pageable pageable);
    List<AnalyticsDTO> getByLotAndRange(Long lotId, LocalDateTime start, LocalDateTime end);
    Double getOccupancyRate(Long lotId);
    Map<Integer, Double> getOccupancyByHour(Long lotId);
    List<Integer> getPeakHours(Long lotId);
    BigDecimal getRevenueByLot(Long lotId, LocalDate start, LocalDate end);
    Map<LocalDate, BigDecimal> getRevenueByDay(Long lotId);
    Map<String, Long> getMostUsedSpotTypes(Long lotId);
    Double getAvgDuration(Long lotId);
    PlatformSummary getPlatformSummary();
    LotReport generateDailyReport(Long lotId, LocalDate date);
    
    // Admin Dashboard
    AdminDashboardDTO getAdminDashboard();
    PlatformSummary getPlatformSummary(LocalDateTime start, LocalDateTime end);
    BigDecimal getTotalRevenue(LocalDateTime start, LocalDateTime end);
    Map<String, BigDecimal> getRevenueByLot(LocalDateTime start, LocalDateTime end);
    Map<String, Long> getBookingStats(LocalDateTime start, LocalDateTime end);
    Map<Integer, Double> getPeakHours(LocalDateTime start, LocalDateTime end);
    Map<String, Object> getLotPerformance(LocalDateTime start, LocalDateTime end);
}
