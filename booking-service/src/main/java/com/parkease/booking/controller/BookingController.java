package com.parkease.booking.controller;

import com.parkease.booking.dto.BookingDTO;
import com.parkease.booking.dto.CreateBookingRequest;
import com.parkease.booking.dto.ExtendBookingRequest;
import com.parkease.booking.entity.Booking;
import com.parkease.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService service;

    @PostMapping
    public ResponseEntity<BookingDTO> create(@RequestHeader("X-User-Id") Long userId,
                                             @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBooking(userId, request));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDTO> get(@PathVariable Long bookingId) {
        return ResponseEntity.ok(service.getBooking(bookingId));
    }

    @GetMapping("/user/me")
    public ResponseEntity<Page<BookingDTO>> myBookings(@RequestHeader("X-User-Id") Long userId, Pageable pageable) {
        return ResponseEntity.ok(service.getUserBookings(userId, pageable));
    }

    @GetMapping("/lot/{lotId}")
    public ResponseEntity<Page<BookingDTO>> lotBookings(@PathVariable Long lotId, Pageable pageable) {
        return ResponseEntity.ok(service.getLotBookings(lotId, pageable));
    }

    @GetMapping("/lot/{lotId}/active")
    public ResponseEntity<java.util.List<BookingDTO>> activeByLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(service.getActiveBookings(lotId));
    }

    @GetMapping("/active")
    public ResponseEntity<java.util.List<BookingDTO>> active(@RequestParam Long lotId) {
        return ResponseEntity.ok(service.getActiveBookings(lotId));
    }

    @GetMapping("/history")
    public ResponseEntity<java.util.List<BookingDTO>> history(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.getBookingHistory(userId));
    }

    @GetMapping("/lot/{lotId}/available")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getAvailableSpots(@PathVariable Long lotId,
                                                                                          @RequestParam LocalDateTime startTime,
                                                                                          @RequestParam LocalDateTime endTime) {
        return ResponseEntity.ok(service.getAvailableSpotsForTime(lotId, startTime, endTime));
    }

    @GetMapping("/spot/{spotId}")
    public ResponseEntity<java.util.List<BookingDTO>> bySpot(@PathVariable Long spotId) {
        return ResponseEntity.ok(service.getBookingsBySpot(spotId));
    }

    @GetMapping("/vehicle/{vehiclePlate}")
    public ResponseEntity<java.util.List<BookingDTO>> byVehiclePlate(@PathVariable String vehiclePlate) {
        return ResponseEntity.ok(service.getBookingsByVehiclePlate(vehiclePlate));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<BookingDTO>> byStatus(@PathVariable Booking.BookingStatus status, Pageable pageable) {
        return ResponseEntity.ok(service.getByStatus(status, pageable));
    }

    @PatchMapping("/{bookingId}/check-in")
    public ResponseEntity<BookingDTO> checkIn(@PathVariable Long bookingId) {
        return ResponseEntity.ok(service.checkIn(bookingId));
    }

    @PutMapping("/{bookingId}/check-in")
    public ResponseEntity<BookingDTO> checkInPut(@PathVariable Long bookingId) {
        return checkIn(bookingId);
    }

    @PatchMapping("/{bookingId}/check-out")
    public ResponseEntity<BookingDTO> checkOut(@PathVariable Long bookingId, @RequestParam(defaultValue = "CASH") String paymentMode) {
        return ResponseEntity.ok(service.checkOut(bookingId, paymentMode));
    }

    @PutMapping("/{bookingId}/check-out")
    public ResponseEntity<BookingDTO> checkOutPut(@PathVariable Long bookingId, @RequestParam(defaultValue = "CASH") String paymentMode) {
        return checkOut(bookingId, paymentMode);
    }

    @PatchMapping("/{bookingId}/extend")
    public ResponseEntity<BookingDTO> extend(@PathVariable Long bookingId, @Valid @RequestBody ExtendBookingRequest request) {
        return ResponseEntity.ok(service.extendBooking(bookingId, request));
    }

    @PutMapping("/{bookingId}/extend")
    public ResponseEntity<BookingDTO> extendPut(@PathVariable Long bookingId, @Valid @RequestBody ExtendBookingRequest request) {
        return extend(bookingId, request);
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long bookingId) {
        service.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelPut(@PathVariable Long bookingId) {
        return cancel(bookingId);
    }

    @GetMapping("/calculate")
    public ResponseEntity<BigDecimal> calculate(@RequestParam Long spotId, @RequestParam LocalDateTime start, @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(service.calculateAmount(spotId, start, end));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Booking Service is running");
    }
}
