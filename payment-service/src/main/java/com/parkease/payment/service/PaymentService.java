package com.parkease.payment.service;

import com.parkease.payment.dto.PaymentDTO;
import com.parkease.payment.dto.ProcessPaymentRequest;
import com.parkease.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PaymentService — business contract for payment processing, refunds,
 * receipt generation, revenue aggregation, and history retrieval.
 *
 * Per ParkEase class diagram (Figure 6): Payment-Service.
 */
public interface PaymentService {

    /** Process a new payment for a booking. */
    PaymentDTO processPayment(Long userId, ProcessPaymentRequest request);

    /** Retrieve a payment by its booking ID. */
    PaymentDTO getByBooking(Long bookingId);

    /** Retrieve all payments for a user (paginated). */
    Page<PaymentDTO> getByUser(Long userId, Pageable pageable);

    /** Retrieve a specific payment by payment ID. */
    PaymentDTO getPayment(Long paymentId);

    /** Retrieve payments filtered by status (paginated). */
    Page<PaymentDTO> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable);

    /** Refund a previously paid payment. Returns the updated payment record. */
    PaymentDTO refundPayment(Long paymentId);

    /** Get the status string of a payment. */
    String getPaymentStatus(Long paymentId);

    /** Manually update the status of a payment (admin/system use). */
    void updateStatus(Long paymentId, Payment.PaymentStatus newStatus);

    /** Generate and return the receipt URL for a payment. */
    String generateReceipt(Long paymentId);

    /** Get total revenue (sum of PAID amounts) for a date range. */
    BigDecimal getTotalRevenue(LocalDateTime start, LocalDateTime end);

    /** Get revenue for a specific lot in a date range. */
    BigDecimal getRevenueByLot(Long lotId, LocalDateTime start, LocalDateTime end);

    /** Get all transactions within a date range. */
    List<PaymentDTO> getTransactionHistory(LocalDateTime start, LocalDateTime end);
}
