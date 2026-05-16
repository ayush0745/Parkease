package com.parkease.payment.dto;

import com.parkease.payment.entity.Payment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO returned by all PaymentResource endpoints.
 * Maps 1-to-1 with the Payment entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Long paymentId;
    private Long bookingId;
    private Long userId;
    private Long lotId;
    private Long spotId;
    private String spotNumber;
    private String lotName;
    private BigDecimal amount;
    private Payment.PaymentStatus status;
    private Payment.PaymentMode mode;
    private String transactionId;
    private String currency;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String receiptUrl;
    private String description;
}
