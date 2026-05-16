package com.parkease.payment.service;

import com.parkease.payment.config.RabbitConfig;
import com.parkease.payment.dto.PaymentDTO;
import com.parkease.payment.dto.ProcessPaymentRequest;
import com.parkease.payment.entity.Payment;
import com.parkease.payment.exception.PaymentNotFoundException;
import com.parkease.payment.mapper.PaymentMapper;
import com.parkease.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final RazorpayPaymentService razorpayPaymentService;
    private final ReceiptService receiptService;
    private final PaymentMapper mapper;
    private final RabbitTemplate rabbitTemplate;

    // -------------------------------------------------------------------------
    // processPayment — idempotent
    // -------------------------------------------------------------------------

    @Override
    public PaymentDTO processPayment(Long userId, ProcessPaymentRequest request) {
        // FIX: removed duplicate findByBookingId + dead ifPresent block
        // Single idempotency check — if already PAID, return existing record
        var existing = repository.findByBookingId(request.getBookingId());
        if (existing.isPresent() && existing.get().getStatus() == Payment.PaymentStatus.PAID) {
            log.info("Idempotent: payment already PAID for bookingId={}", request.getBookingId());
            return mapper.toDto(existing.get());
        }

// cardToken validation removed — Razorpay doesn't use card tokens on backend
        // Frontend handles card details via Razorpay checkout JS

        Payment payment = Payment.builder()
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

        try {
            String transactionId = resolveTransactionId(request);
            payment.setTransactionId(transactionId);
            payment.setStatus(Payment.PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());

            Payment saved = repository.save(payment);

            // Generate PDF receipt after paidAt is set (never null here)
            String receiptUrl = receiptService.generateReceipt(
                    saved.getBookingId(), saved.getPaymentId(),
                    saved.getAmount(), saved.getPaidAt());
            saved.setReceiptUrl(receiptUrl);
            saved = repository.save(saved);

            publishEvent("payment.paid", saved);
            log.info("Payment processed: paymentId={}, bookingId={}, amount={}{}",
                    saved.getPaymentId(), saved.getBookingId(),
                    saved.getCurrency(), saved.getAmount());
            return mapper.toDto(saved);

        } catch (IllegalArgumentException ex) {
            // Validation errors — don't persist a FAILED record
            throw ex;
        } catch (Exception ex) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            Payment failed = repository.save(payment);
            publishEvent("payment.failed", failed);
            log.error("Payment failed for bookingId={}: {}", request.getBookingId(), ex.getMessage());
            throw new IllegalStateException("Payment processing failed: " + ex.getMessage());
        }
    }

    private String resolveTransactionId(ProcessPaymentRequest request) throws Exception {
        return switch (request.getMode()) {
            case CARD   -> razorpayPaymentService.processPayment(
                               request.getAmount(),
                               "ParkEase booking #" + request.getBookingId());
            case CASH   -> "CASH-"   + UUID.randomUUID();
            case UPI    -> razorpayPaymentService.processPayment(
                               request.getAmount(),
                               "ParkEase UPI booking #" + request.getBookingId());
            case WALLET -> razorpayPaymentService.processPayment(
                               request.getAmount(),
                               "ParkEase wallet booking #" + request.getBookingId());
        };
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPayment(Long paymentId) {
        return repository.findById(paymentId)
                .map(mapper::toDto)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getByBooking(Long bookingId) {
        return repository.findByBookingId(bookingId)
                .map(mapper::toDto)
                .orElseThrow(() -> new PaymentNotFoundException("No payment for bookingId=" + bookingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> getByUser(Long userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(mapper::toDto);
    }

    // -------------------------------------------------------------------------
    // Status
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public String getPaymentStatus(Long paymentId) {
        return repository.findById(paymentId)
                .map(p -> p.getStatus().name())
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    @Override
    public void updateStatus(Long paymentId, Payment.PaymentStatus newStatus) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        Payment.PaymentStatus old = payment.getStatus();
        payment.setStatus(newStatus);
        repository.save(payment);
        log.info("Status updated: paymentId={}, {} -> {}", paymentId, old, newStatus);
        publishEvent("payment.status.updated", payment);
    }

    // -------------------------------------------------------------------------
    // Refund
    // -------------------------------------------------------------------------

    @Override
    public PaymentDTO refundPayment(Long paymentId) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.PAID) {
            throw new IllegalStateException(
                "Only PAID payments can be refunded. Current status: " + payment.getStatus());
        }

        if (payment.getMode() == Payment.PaymentMode.CARD ||
            payment.getMode() == Payment.PaymentMode.UPI ||
            payment.getMode() == Payment.PaymentMode.WALLET) {
            try {
                razorpayPaymentService.refundPayment(payment.getTransactionId(), payment.getAmount());
            } catch (Exception ex) {
                throw new IllegalStateException("Razorpay refund failed: " + ex.getMessage());
            }
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        Payment saved = repository.save(payment);
        publishEvent("payment.refunded", saved);
        log.info("Refund issued: paymentId={}, mode={}", paymentId, payment.getMode());
        return mapper.toDto(saved);
    }

    // -------------------------------------------------------------------------
    // Receipt
    // -------------------------------------------------------------------------

    @Override
    public String generateReceipt(Long paymentId) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // FIX: guard against null paidAt — can only generate receipt for PAID payments
        if (payment.getStatus() != Payment.PaymentStatus.PAID) {
            throw new IllegalStateException(
                "Receipt can only be generated for PAID payments. Status: " + payment.getStatus());
        }

        if (payment.getReceiptUrl() != null && !payment.getReceiptUrl().isBlank()) {
            java.io.File file = new java.io.File(payment.getReceiptUrl());
            if (file.exists() && file.isFile()) {
                return payment.getReceiptUrl(); // Already generated and exists — return cached path
            }
        }

        String receiptUrl = receiptService.generateReceipt(
                payment.getBookingId(), payment.getPaymentId(),
                payment.getAmount(), payment.getPaidAt());   // paidAt guaranteed non-null here
        payment.setReceiptUrl(receiptUrl);
        repository.save(payment);
        return receiptUrl;
    }

    // -------------------------------------------------------------------------
    // Revenue & history
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue(LocalDateTime start, LocalDateTime end) {
        return repository.getTotalRevenue(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueByLot(Long lotId, LocalDateTime start, LocalDateTime end) {
        return repository.sumAmountByLotId(lotId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getTransactionHistory(LocalDateTime start, LocalDateTime end) {
        return repository.findByPaidAtBetween(start, end).stream()
                .map(mapper::toDto)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Event publishing — fire-and-forget, never breaks payment flow
    // -------------------------------------------------------------------------

    private void publishEvent(String routingKey, Payment payment) {
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("eventType", routingKey);
            event.put("paymentId", payment.getPaymentId());
            event.put("bookingId", payment.getBookingId());
            event.put("userId", payment.getUserId());
            event.put("lotId", payment.getLotId());
            event.put("amount", payment.getAmount().toPlainString());
            event.put("currency", payment.getCurrency());
            event.put("status", payment.getStatus().name());
            event.put("timestamp", LocalDateTime.now().toString());
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
        } catch (Exception ex) {
            log.warn("Failed to publish event '{}': {}", routingKey, ex.getMessage());
        }
    }
}
