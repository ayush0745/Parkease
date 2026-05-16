package com.parkease.booking.service;

import com.parkease.booking.entity.Booking;
import com.parkease.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingSchedulerService {
    
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository
                .findExpiredReservedBookings(now.minusMinutes(15)); // 15 min grace period
        
        for (Booking booking : expiredBookings) {
            try {
                log.info("Auto-canceling expired booking: {}", booking.getBookingId());
                bookingService.cancelBooking(booking.getBookingId());
            } catch (Exception e) {
                log.error("Failed to auto-cancel booking {}: {}", booking.getBookingId(), e.getMessage());
            }
        }
    }
    
    @Scheduled(fixedRate = 900000) // Every 15 minutes
    @Transactional
    public void sendExpiryReminders() {
        LocalDateTime reminderTime = LocalDateTime.now().plusMinutes(30);
        List<Booking> upcomingExpiry = bookingRepository
                .findBookingsExpiringBetween(LocalDateTime.now(), reminderTime);
        
        for (Booking booking : upcomingExpiry) {
            // TODO: Send notification via RabbitMQ
            log.info("Sending expiry reminder for booking: {}", booking.getBookingId());
        }
    }
}