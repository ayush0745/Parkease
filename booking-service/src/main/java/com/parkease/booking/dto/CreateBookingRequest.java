package com.parkease.booking.dto;

import com.parkease.booking.entity.Booking;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateBookingRequest {
    @NotNull
    private Long lotId;
    @NotNull
    private Long spotId;
    @NotBlank
    private String vehiclePlate;
    @NotNull
    private Booking.VehicleType vehicleType;
    @NotNull
    private Booking.BookingType bookingType;
    @NotNull
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String paymentMethod;
}
