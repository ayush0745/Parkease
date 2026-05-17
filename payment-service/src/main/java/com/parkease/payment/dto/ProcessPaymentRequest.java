package com.parkease.payment.dto;

import com.parkease.payment.entity.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request payload for POST /api/v1/payments/process.
 *
 * cardToken is required only when mode = CARD (Stripe token).
 * lotId is provided by the booking-service so payment can be
 * stored for lot-level revenue aggregation.
 */
@Data
public class ProcessPaymentRequest {

    @NotNull(message = "bookingId is required")
    private Long bookingId;

    /** The parking lot associated with this booking — used for revenue aggregation. */
    private Long lotId;
    private Long spotId;
    private String spotNumber;
    private String lotName;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "mode is required")
    private Payment.PaymentMode mode;

    /** Required only for CARD payments — Stripe tokenized card token. */
    private String cardToken;

    /** ISO 4217 currency code. Defaults to INR. */
    private String currency = "INR";

    /** Optional human-readable note, e.g. "Parking fee - Lot 5, Spot A12". */
    private String description;
}
