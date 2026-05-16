package com.parkease.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPaymentRequest {
    private Long bookingId;
    private Long lotId;
    private Long spotId;
    private String spotNumber;
    private String lotName;
    private BigDecimal amount;
    private String mode;
    private String currency;
}
