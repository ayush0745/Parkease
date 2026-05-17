package com.parkease.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity — linked to exactly one booking, records amount, mode,
 * transaction ID, currency, and status.
 *
 * Per ParkEase class diagram (Figure 6):
 *   paymentId, bookingId, userId, amount, status, mode,
 *   transactionId, currency, paidAt, refundedAt, description
 *
 * lotId added to enable lot-level revenue aggregation (sumAmountByLotId).
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_booking",     columnList = "bookingId"),
        @Index(name = "idx_payment_user",        columnList = "userId"),
        @Index(name = "idx_payment_lot",         columnList = "lotId"),
        @Index(name = "idx_payment_status",      columnList = "status"),
        @Index(name = "idx_payment_paid_at",     columnList = "paidAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    /** The booking this payment is associated with (1-to-1). */
    @Column(nullable = false)
    private Long bookingId;

    /** The driver making the payment. */
    @Column(nullable = false)
    private Long userId;

    /**
     * The parking lot this booking belongs to.
     * Stored here to enable lot-level revenue aggregation without
     * a cross-service JOIN at query time.
     */
    @Column
    private Long lotId;

    @Column
    private Long spotId;

    @Column
    private String spotNumber;

    @Column
    private String lotName;

    /** Actual amount charged in the specified currency. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** Payment mode chosen by the driver. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentMode mode;

    /** Gateway transaction ID (Stripe charge ID, UPI ref, etc.). */
    private String transactionId;

    /** ISO 4217 currency code. Default: INR (Indian Rupee). */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    /** Timestamp when payment was confirmed PAID. */
    private LocalDateTime paidAt;

    /** Timestamp when a refund was issued. */
    private LocalDateTime refundedAt;

    /** File path / URL of the generated PDF receipt. */
    private String receiptUrl;

    /** Human-readable description (e.g. "ParkEase booking #42"). */
    private String description;

    // -------------------------------------------------------------------------
    // Enums
    // -------------------------------------------------------------------------

    public enum PaymentStatus {
        PENDING,   // Payment initiated but not yet confirmed
        PAID,      // Successfully processed
        REFUNDED,  // Refund issued
        FAILED     // Processing failed
    }

    public enum PaymentMode {
        CARD,    // Debit/credit card via Stripe
        UPI,     // Unified Payments Interface (India)
        WALLET,  // ParkEase in-app wallet
        CASH     // Pay on exit at gate (COD)
    }
}
