package com.parkease.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    private String eventType;
    private Long bookingId;
    private Long userId;
    private Long lotId;
    private Long spotId;
    private String vehiclePlate;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String userEmail;
    private String lotName;
    private String spotNumber;
}