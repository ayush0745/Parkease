package com.parkease.booking.repository;

import com.parkease.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    List<Booking> findByLotId(Long lotId);
    Page<Booking> findByLotId(Long lotId, Pageable pageable);
    List<Booking> findBySpotId(Long spotId);
    List<Booking> findByStatus(Booking.BookingStatus status);
    Page<Booking> findByStatus(Booking.BookingStatus status, Pageable pageable);
    Optional<Booking> findByBookingId(Long bookingId);
    List<Booking> findByVehiclePlate(String vehiclePlate);

    @Query("SELECT b FROM Booking b WHERE b.spotId = :spotId AND b.status = 'ACTIVE'")
    Optional<Booking> findActiveBySpotId(@Param("spotId") Long spotId);

    @Query("SELECT b FROM Booking b WHERE b.spotId = :spotId AND b.status IN ('RESERVED','ACTIVE') AND b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findConflictingBookings(@Param("spotId") Long spotId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT b FROM Booking b WHERE b.bookingType = 'PRE' AND b.status = 'RESERVED' AND b.startTime < :cutoff")
    List<Booking> findExpiredPreBookings(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT b FROM Booking b WHERE b.status = 'RESERVED' AND b.startTime < :expiredTime")
    List<Booking> findExpiredReservedBookings(@Param("expiredTime") LocalDateTime expiredTime);
    
    @Query("SELECT b FROM Booking b WHERE b.status IN ('RESERVED', 'ACTIVE') AND " +
           "b.endTime BETWEEN :start AND :end")
    List<Booking> findBookingsExpiringBetween(@Param("start") LocalDateTime start, 
                                             @Param("end") LocalDateTime end);

    long countByLotIdAndStatus(Long lotId, Booking.BookingStatus status);
}
