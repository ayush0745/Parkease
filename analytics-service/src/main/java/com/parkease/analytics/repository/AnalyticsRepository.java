package com.parkease.analytics.repository;

import com.parkease.analytics.entity.OccupancyLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends JpaRepository<OccupancyLog, Long> {
    List<OccupancyLog> findByLotId(Long lotId);
    Page<OccupancyLog> findByLotId(Long lotId, Pageable pageable);
    List<OccupancyLog> findByLotIdAndTimestampBetween(Long lotId, LocalDateTime start, LocalDateTime end);
    List<OccupancyLog> findByVehicleType(OccupancyLog.VehicleType vehicleType);
    Optional<OccupancyLog> findTopByLotIdOrderByTimestampDesc(Long lotId);
    long countByLotIdAndTimestampBetween(Long lotId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(o.occupancyRate) FROM OccupancyLog o WHERE o.lotId = :lotId")
    Double avgOccupancyByLotId(@Param("lotId") Long lotId);

    @Query("SELECT HOUR(o.timestamp), AVG(o.occupancyRate) FROM OccupancyLog o WHERE o.lotId = :lotId GROUP BY HOUR(o.timestamp) ORDER BY HOUR(o.timestamp)")
    List<Object[]> occupancyByHour(@Param("lotId") Long lotId);

    @Query("SELECT HOUR(o.timestamp), AVG(o.occupancyRate) FROM OccupancyLog o WHERE o.lotId = :lotId GROUP BY HOUR(o.timestamp) ORDER BY AVG(o.occupancyRate) DESC")
    List<Object[]> findPeakHoursByLotId(@Param("lotId") Long lotId);

    @Query("SELECT COUNT(o) FROM OccupancyLog o WHERE o.lotId = :lotId AND o.timestamp >= CURRENT_DATE")
    int countByLotIdToday(@Param("lotId") Long lotId);

    @Query("""
            SELECT COALESCE(SUM(o.revenue), 0)
            FROM OccupancyLog o
            WHERE o.lotId = :lotId
              AND o.eventType = 'payment.paid'
              AND o.timestamp BETWEEN :start AND :end
            """)
    BigDecimal sumRevenueByLot(@Param("lotId") Long lotId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Query("""
            SELECT DATE(o.timestamp), COALESCE(SUM(o.revenue), 0)
            FROM OccupancyLog o
            WHERE o.lotId = :lotId
              AND o.eventType = 'payment.paid'
            GROUP BY DATE(o.timestamp)
            ORDER BY DATE(o.timestamp)
            """)
    List<Object[]> revenueByDay(@Param("lotId") Long lotId);

    @Query("""
            SELECT COALESCE(o.spotType, 'UNKNOWN'), COUNT(o)
            FROM OccupancyLog o
            WHERE o.lotId = :lotId
              AND o.eventType IN ('booking.created', 'booking.checkin', 'booking.checkout')
            GROUP BY COALESCE(o.spotType, 'UNKNOWN')
            ORDER BY COUNT(o) DESC
            """)
    List<Object[]> mostUsedSpotTypes(@Param("lotId") Long lotId);

    @Query("""
            SELECT AVG(o.durationMinutes)
            FROM OccupancyLog o
            WHERE o.lotId = :lotId
              AND o.durationMinutes IS NOT NULL
              AND o.durationMinutes > 0
            """)
    Double avgDurationByLot(@Param("lotId") Long lotId);

    @Query("SELECT COUNT(DISTINCT o.lotId) FROM OccupancyLog o")
    long countActiveLots();

    @Query("SELECT AVG(o.occupancyRate) FROM OccupancyLog o")
    Double avgOccupancyPlatform();

    @Query("SELECT COALESCE(SUM(o.revenue), 0) FROM OccupancyLog o WHERE o.eventType = 'payment.paid'")
    BigDecimal totalRevenue();

    @Query("SELECT AVG(o.durationMinutes) FROM OccupancyLog o WHERE o.durationMinutes IS NOT NULL AND o.durationMinutes > 0")
    Double avgDurationPlatform();
    
    // Additional methods for admin dashboard
    @Query("SELECT COALESCE(SUM(o.revenue), 0) FROM OccupancyLog o WHERE o.eventType = 'payment.paid' AND o.timestamp BETWEEN :start AND :end")
    BigDecimal sumRevenueByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(o) FROM OccupancyLog o WHERE o.timestamp BETWEEN :start AND :end")
    long countByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT AVG(o.occupancyRate) FROM OccupancyLog o WHERE o.timestamp BETWEEN :start AND :end")
    Double avgOccupancyByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT AVG(o.durationMinutes) FROM OccupancyLog o WHERE o.durationMinutes IS NOT NULL AND o.durationMinutes > 0 AND o.timestamp BETWEEN :start AND :end")
    Double avgDurationByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT o.lotId, COALESCE(SUM(o.revenue), 0) FROM OccupancyLog o WHERE o.eventType = 'payment.paid' AND o.timestamp BETWEEN :start AND :end GROUP BY o.lotId")
    List<Object[]> revenueByLotAndDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(o) FROM OccupancyLog o WHERE o.eventType = :eventType AND o.timestamp BETWEEN :start AND :end")
    long countByEventTypeAndTimestampBetween(@Param("eventType") String eventType, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT HOUR(o.timestamp), AVG(o.occupancyRate) FROM OccupancyLog o WHERE o.timestamp BETWEEN :start AND :end GROUP BY HOUR(o.timestamp) ORDER BY HOUR(o.timestamp)")
    List<Object[]> occupancyByHourAndDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
