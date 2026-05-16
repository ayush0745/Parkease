package com.parkease.payment.repository;

import com.parkease.payment.entity.Payment;
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

/**
 * PaymentRepository — data access for Payment entities.
 *
 * Per ParkEase class diagram (Figure 6):
 *   findByBookingId, findByUserId, findByStatus, findByTransactionId,
 *   findByPaidAtBetween, sumAmountByLotId, countByUserId
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    List<Payment> findByUserId(Long userId);

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);

    long countByUserId(Long userId);

    /**
     * Platform-wide revenue: total PAID amount in a date range.
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.paidAt BETWEEN :start AND :end")
    BigDecimal getTotalRevenue(@Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    /**
     * Lot-level revenue aggregation: sum of PAID amounts for bookings
     * belonging to a specific parking lot, within a date range.
     * NOTE: lotId is resolved via the booking-service; here we aggregate
     * by the lotId stored on the payment record.
     *
     * Per class diagram: sumAmountByLotId(int):Double
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.lotId = :lotId AND p.status = 'PAID' " +
           "AND p.paidAt BETWEEN :start AND :end")
    BigDecimal sumAmountByLotId(@Param("lotId") Long lotId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
}
