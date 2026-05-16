package com.parkease.payment.mapper;

import com.parkease.payment.dto.PaymentDTO;
import com.parkease.payment.dto.ProcessPaymentRequest;
import com.parkease.payment.entity.Payment;
import org.springframework.stereotype.Component;

/**
 * PaymentMapper — converts between Payment entity and PaymentDTO.
 */
@Component
public class PaymentMapper {

    public PaymentDTO toDto(Payment payment) {
        return PaymentDTO.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .lotId(payment.getLotId())
                .spotId(payment.getSpotId())
                .spotNumber(payment.getSpotNumber())
                .lotName(payment.getLotName())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .mode(payment.getMode())
                .transactionId(payment.getTransactionId())
                .currency(payment.getCurrency())
                .paidAt(payment.getPaidAt())
                .refundedAt(payment.getRefundedAt())
                .receiptUrl(payment.getReceiptUrl())
                .description(payment.getDescription())
                .build();
    }

    public Payment toEntity(ProcessPaymentRequest request, Long userId) {
        return Payment.builder()
                .bookingId(request.getBookingId())
                .lotId(request.getLotId())
                .spotId(request.getSpotId())
                .spotNumber(request.getSpotNumber())
                .lotName(request.getLotName())
                .userId(userId)
                .amount(request.getAmount())
                .mode(request.getMode())
                .currency(request.getCurrency() == null ? "INR" : request.getCurrency())
                .description(request.getDescription())
                .status(Payment.PaymentStatus.PENDING)
                .build();
    }
}
