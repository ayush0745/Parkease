package com.parkease.booking.dto;

import com.parkease.booking.entity.Booking;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long bookingId;
    private Long userId;
    private Long lotId;
    private Long spotId;
    private String vehiclePlate;
    private Booking.VehicleType vehicleType;
    private Booking.BookingType bookingType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Booking.BookingStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
