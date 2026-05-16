package com.parkease.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExtendBookingRequest {
    @NotNull
    @Future
    private LocalDateTime newEndTime;
}
