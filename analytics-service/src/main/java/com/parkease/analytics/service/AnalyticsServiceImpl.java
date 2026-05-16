package com.parkease.analytics.service;

import com.parkease.analytics.dto.AdminDashboardDTO;
import com.parkease.analytics.dto.AnalyticsDTO;
import com.parkease.analytics.dto.LotReport;
import com.parkease.analytics.dto.PlatformSummary;
import com.parkease.analytics.entity.OccupancyLog;
import com.parkease.analytics.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository repository;

    @Override
    public AnalyticsDTO logOccupancy(AnalyticsDTO dto) {
        validateLot(dto.getLotId());
        int total = positiveOrDefault(dto.getTotalSpots(), latestTotal(dto.getLotId()));
        int available = clamp(dto.getAvailableSpots() == null ? total : dto.getAvailableSpots(), 0, total);
        double rate = dto.getOccupancyRate() == null ? occupancyRate(total, available) : dto.getOccupancyRate();

        OccupancyLog saved = repository.save(OccupancyLog.builder()
                .lotId(dto.getLotId())
                .spotId(dto.getSpotId())
                .timestamp(dto.getTimestamp() == null ? LocalDateTime.now() : dto.getTimestamp())
                .occupancyRate(rate)
                .availableSpots(available)
                .totalSpots(total)
                .vehicleType(dto.getVehicleType())
                .spotType(blankToNull(dto.getSpotType()))
                .eventType(blankToNull(dto.getEventType()))
                .bookingId(dto.getBookingId())
                .amount(dto.getAmount())
                .currency(blankToNull(dto.getCurrency()))
                .durationMinutes(dto.getDurationMinutes())
                .build());
        log.debug("Logged analytics snapshot id={} lotId={} rate={}", saved.getLogId(), saved.getLotId(), saved.getOccupancyRate());
        return toDto(saved);
    }

    @Override
    public void logOccupancy(Long lotId, Long spotId) {
        validateLot(lotId);
        OccupancyLog latest = repository.findTopByLotIdOrderByTimestampDesc(lotId).orElse(null);
        int total = latest == null ? 1 : latest.getTotalSpots();
        int available = latest == null ? total : latest.getAvailableSpots();
        logOccupancy(AnalyticsDTO.builder()
                .lotId(lotId)
                .spotId(spotId)
                .totalSpots(total)
                .availableSpots(available)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AnalyticsDTO> getLog(Long logId) {
        return repository.findById(logId).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnalyticsDTO> getByLot(Long lotId, Pageable pageable) {
        validateLot(lotId);
        return repository.findByLotId(lotId, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalyticsDTO> getByLotAndRange(Long lotId, LocalDateTime start, LocalDateTime end) {
        validateLot(lotId);
        validateRange(start, end);
        return repository.findByLotIdAndTimestampBetween(lotId, start, end).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getOccupancyRate(Long lotId) {
        validateLot(lotId);
        return repository.findTopByLotIdOrderByTimestampDesc(lotId)
                .map(OccupancyLog::getOccupancyRate)
                .orElseGet(() -> Optional.ofNullable(repository.avgOccupancyByLotId(lotId)).orElse(0.0));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Double> getOccupancyByHour(Long lotId) {
        validateLot(lotId);
        return rowsToHourMap(repository.occupancyByHour(lotId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getPeakHours(Long lotId) {
        validateLot(lotId);
        return repository.findPeakHoursByLotId(lotId).stream()
                .limit(3)
                .map(row -> ((Number) row[0]).intValue())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueByLot(Long lotId, LocalDate start, LocalDate end) {
        validateLot(lotId);
        LocalDateTime startDateTime = (start == null ? LocalDate.now() : start).atStartOfDay();
        LocalDateTime endDateTime = (end == null ? LocalDate.now() : end).plusDays(1).atStartOfDay();
        validateRange(startDateTime, endDateTime);
        return nullToZero(repository.sumRevenueByLot(lotId, startDateTime, endDateTime));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, BigDecimal> getRevenueByDay(Long lotId) {
        validateLot(lotId);
        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : repository.revenueByDay(lotId)) {
            result.put(toLocalDate(row[0]), nullToZero((BigDecimal) row[1]));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getMostUsedSpotTypes(Long lotId) {
        validateLot(lotId);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : repository.mostUsedSpotTypes(lotId)) {
            result.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAvgDuration(Long lotId) {
        validateLot(lotId);
        return Optional.ofNullable(repository.avgDurationByLot(lotId)).orElse(0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public PlatformSummary getPlatformSummary() {
        return PlatformSummary.builder()
                .totalLogs(repository.count())
                .activeLots(repository.countActiveLots())
                .averageOccupancyRate(Optional.ofNullable(repository.avgOccupancyPlatform()).orElse(0.0))
                .totalRevenue(nullToZero(repository.totalRevenue()))
                .averageDurationMinutes(Optional.ofNullable(repository.avgDurationPlatform()).orElse(0.0))
                .generatedAt(LocalDateTime.now())
                .build();
    }
    
    @Override
    public AdminDashboardDTO getAdminDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        return AdminDashboardDTO.builder()
                .totalUsers(getTotalUsers())
                .totalDrivers(getUsersByRole("DRIVER"))
                .totalManagers(getUsersByRole("MANAGER"))
                .totalLots(getTotalLots())
                .approvedLots(getApprovedLots())
                .pendingLots(getPendingLots())
                .totalSpots(getTotalSpots())
                .availableSpots(getAvailableSpots())
                .totalBookings(getTotalBookings())
                .activeBookings(getActiveBookings())
                .completedBookings(getCompletedBookings())
                .totalRevenue(nullToZero(repository.totalRevenue()))
                .todayRevenue(getTodayRevenue(startOfDay, now))
                .monthlyRevenue(getMonthlyRevenue(startOfMonth, now))
                .averageOccupancy(Optional.ofNullable(repository.avgOccupancyPlatform()).orElse(0.0))
                .lastUpdated(now)
                .build();
    }
    
    private Long getTotalUsers() {
        return 0L; // Placeholder - would call auth service
    }
    
    private Long getUsersByRole(String role) {
        return 0L; // Placeholder - would call auth service
    }
    
    private Long getTotalLots() {
        return repository.countActiveLots();
    }
    
    private Long getApprovedLots() {
        return repository.countActiveLots();
    }
    
    private Long getPendingLots() {
        return 0L; // Placeholder - would call parking-lot service
    }
    
    private Long getTotalSpots() {
        return 0L; // Placeholder - would call spot service
    }
    
    private Long getAvailableSpots() {
        return 0L; // Placeholder - would call spot service
    }
    
    private Long getTotalBookings() {
        return repository.count();
    }
    
    private Long getActiveBookings() {
        return 0L; // Placeholder - would call booking service
    }
    
    private Long getCompletedBookings() {
        return 0L; // Placeholder - would call booking service
    }
    
    private BigDecimal getTodayRevenue(LocalDateTime start, LocalDateTime end) {
        return nullToZero(repository.sumRevenueByDateRange(start, end));
    }
    
    private BigDecimal getMonthlyRevenue(LocalDateTime start, LocalDateTime end) {
        return nullToZero(repository.sumRevenueByDateRange(start, end));
    }
    
    @Override
    public PlatformSummary getPlatformSummary(LocalDateTime start, LocalDateTime end) {
        return PlatformSummary.builder()
                .totalLogs(repository.countByTimestampBetween(start, end))
                .activeLots(repository.countActiveLots())
                .averageOccupancyRate(Optional.ofNullable(repository.avgOccupancyByDateRange(start, end)).orElse(0.0))
                .totalRevenue(nullToZero(repository.sumRevenueByDateRange(start, end)))
                .averageDurationMinutes(Optional.ofNullable(repository.avgDurationByDateRange(start, end)).orElse(0.0))
                .generatedAt(LocalDateTime.now())
                .build();
    }
    
    @Override
    public BigDecimal getTotalRevenue(LocalDateTime start, LocalDateTime end) {
        return nullToZero(repository.sumRevenueByDateRange(start, end));
    }
    
    @Override
    public Map<String, BigDecimal> getRevenueByLot(LocalDateTime start, LocalDateTime end) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : repository.revenueByLotAndDateRange(start, end)) {
            result.put("Lot " + row[0], nullToZero((BigDecimal) row[1]));
        }
        return result;
    }
    
    @Override
    public Map<String, Long> getBookingStats(LocalDateTime start, LocalDateTime end) {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("total", repository.countByTimestampBetween(start, end));
        result.put("checkins", repository.countByEventTypeAndTimestampBetween("CHECK_IN", start, end));
        result.put("checkouts", repository.countByEventTypeAndTimestampBetween("CHECK_OUT", start, end));
        return result;
    }
    
    @Override
    public Map<Integer, Double> getPeakHours(LocalDateTime start, LocalDateTime end) {
        return rowsToHourMap(repository.occupancyByHourAndDateRange(start, end));
    }
    
    @Override
    public Map<String, Object> getLotPerformance(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRevenue", getTotalRevenue(start, end));
        result.put("revenueByLot", getRevenueByLot(start, end));
        result.put("averageOccupancy", Optional.ofNullable(repository.avgOccupancyByDateRange(start, end)).orElse(0.0));
        result.put("peakHours", getPeakHours(start, end));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public LotReport generateDailyReport(Long lotId, LocalDate date) {
        validateLot(lotId);
        LocalDate reportDate = date == null ? LocalDate.now() : date;
        LocalDateTime start = reportDate.atStartOfDay();
        LocalDateTime end = reportDate.plusDays(1).atStartOfDay();
        long logCount = repository.countByLotIdAndTimestampBetween(lotId, start, end);
        return LotReport.builder()
                .lotId(lotId)
                .date(reportDate)
                .logCount(logCount)
                .occupancyRate(getOccupancyRate(lotId))
                .occupancyByHour(getOccupancyByHour(lotId))
                .peakHours(getPeakHours(lotId))
                .revenue(getRevenueByLot(lotId, reportDate, reportDate))
                .revenueByDay(getRevenueByDay(lotId))
                .mostUsedSpotTypes(getMostUsedSpotTypes(lotId))
                .averageDurationMinutes(getAvgDuration(lotId))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private Map<Integer, Double> rowsToHourMap(List<Object[]> rows) {
        Map<Integer, Double> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }
        return result;
    }

    private AnalyticsDTO toDto(OccupancyLog entity) {
        return AnalyticsDTO.builder()
                .logId(entity.getLogId())
                .lotId(entity.getLotId())
                .spotId(entity.getSpotId())
                .timestamp(entity.getTimestamp())
                .occupancyRate(entity.getOccupancyRate())
                .availableSpots(entity.getAvailableSpots())
                .totalSpots(entity.getTotalSpots())
                .vehicleType(entity.getVehicleType())
                .spotType(entity.getSpotType())
                .eventType(entity.getEventType())
                .bookingId(entity.getBookingId())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .durationMinutes(entity.getDurationMinutes())
                .build();
    }

    private int latestTotal(Long lotId) {
        return repository.findTopByLotIdOrderByTimestampDesc(lotId).map(OccupancyLog::getTotalSpots).orElse(1);
    }

    private double occupancyRate(int total, int available) {
        return total == 0 ? 0.0 : ((total - available) * 100.0 / total);
    }

    private int positiveOrDefault(Integer value, int defaultValue) {
        return value == null || value <= 0 ? Math.max(1, defaultValue) : value;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void validateLot(Long lotId) {
        if (lotId == null || lotId <= 0) {
            throw new IllegalArgumentException("lotId must be a positive number");
        }
    }

    private void validateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) return localDate;
        if (value instanceof Date date) return date.toLocalDate();
        return LocalDate.parse(String.valueOf(value));
    }
}
