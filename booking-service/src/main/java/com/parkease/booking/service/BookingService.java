package com.parkease.booking.service;

import com.parkease.booking.dto.BookingDTO;
import com.parkease.booking.dto.CreateBookingRequest;
import com.parkease.booking.dto.ExtendBookingRequest;
import com.parkease.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    BookingDTO createBooking(Long userId, CreateBookingRequest request);
    BookingDTO getBookingById(Long bookingId);
    BookingDTO getBooking(Long bookingId);
    Page<BookingDTO> getUserBookings(Long userId, Pageable pageable);
    Page<BookingDTO> getLotBookings(Long lotId, Pageable pageable);
    Page<BookingDTO> getByStatus(Booking.BookingStatus status, Pageable pageable);
    List<java.util.Map<String, Object>> getAvailableSpotsForTime(Long lotId, LocalDateTime start, LocalDateTime end);
    List<BookingDTO> getActiveBookings(Long lotId);
    List<BookingDTO> getBookingHistory(Long userId);
    List<BookingDTO> getBookingsBySpot(Long spotId);
    List<BookingDTO> getBookingsByVehiclePlate(String vehiclePlate);
    void cancelBooking(Long bookingId);
    BookingDTO checkIn(Long bookingId);
    BookingDTO checkOut(Long bookingId, String paymentMode);
    BookingDTO extendBooking(Long bookingId, ExtendBookingRequest request);
    BigDecimal calculateAmount(Long spotId, LocalDateTime start, LocalDateTime end);
}
