package com.parkease.payment.controller;

import com.parkease.payment.dto.PaymentDTO;
import com.parkease.payment.dto.ProcessPaymentRequest;
import com.parkease.payment.entity.Payment;
import com.parkease.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PaymentResource (PaymentController) — REST API for Payment-Service.
 *
 * Base path: /api/v1/payments
 *
 * Per ParkEase class diagram (Figure 6):
 *   POST   /process          → processPayment
 *   GET    /{id}             → getPayment
 *   GET    /booking/{id}     → getByBooking
 *   GET    /user/me          → getByUser
 *   POST   /{id}/refund      → refundPayment
 *   GET    /{id}/status      → getPaymentStatus
 *   GET    /{id}/receipt     → generateReceipt
 *   GET    /revenue          → getTotalRevenue
 *   GET    /revenue/lot/{id} → getRevenueByLot
 *   GET    /history          → getTransactionHistory
 *   PUT    /{id}/status      → updateStatus  (admin)
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing, refunds, receipts and revenue reporting")
public class PaymentController {

    private final PaymentService service;

    // -------------------------------------------------------------------------
    // Process payment
    // -------------------------------------------------------------------------

    @PostMapping("/process")
    @Operation(summary = "Process a payment", description = "Processes a parking fee payment for a booking. Idempotent — returns existing record if booking is already PAID.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "409", description = "Payment processing conflict")
    })
    public ResponseEntity<PaymentDTO> process(
            @Parameter(description = "Authenticated user ID (injected by gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ProcessPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.processPayment(userId, request));
    }

    // -------------------------------------------------------------------------
    // Fetch payments
    // -------------------------------------------------------------------------

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentDTO> get(@PathVariable Long paymentId,
                                          @RequestHeader("X-User-Id") Long userId,
                                          @RequestHeader(value = "X-User-Role", required = false) String role) {
        PaymentDTO payment = service.getPayment(paymentId);
        requireOwnerOrAdmin(payment, userId, role);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment by booking ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "No payment for this booking")
    })
    public ResponseEntity<PaymentDTO> byBooking(@PathVariable Long bookingId,
                                                @RequestHeader("X-User-Id") Long userId,
                                                @RequestHeader(value = "X-User-Role", required = false) String role) {
        PaymentDTO payment = service.getByBooking(bookingId);
        requireOwnerOrAdmin(payment, userId, role);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/user/me")
    @Operation(summary = "Get all payments for the authenticated user (paginated)")
    public ResponseEntity<Page<PaymentDTO>> mine(
            @RequestHeader("X-User-Id") Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(service.getByUser(userId, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status (paginated) — admin use")
    public ResponseEntity<Page<PaymentDTO>> byStatus(
            @PathVariable Payment.PaymentStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            Pageable pageable) {
        requireAdmin(role);
        return ResponseEntity.ok(service.getPaymentsByStatus(status, pageable));
    }

    // -------------------------------------------------------------------------
    // Refund
    // -------------------------------------------------------------------------

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment", description = "Triggers a refund for a PAID payment. For CARD payments, issues a Stripe refund.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund processed"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "409", description = "Payment is not in PAID state")
    })
    public ResponseEntity<PaymentDTO> refund(@PathVariable Long paymentId,
                                             @RequestHeader("X-User-Id") Long userId,
                                             @RequestHeader(value = "X-User-Role", required = false) String role) {
        requireOwnerOrAdmin(service.getPayment(paymentId), userId, role);
        return ResponseEntity.ok(service.refundPayment(paymentId));
    }

    // -------------------------------------------------------------------------
    // Status
    // -------------------------------------------------------------------------

    @GetMapping("/{paymentId}/status")
    @Operation(summary = "Get payment status string")
    public ResponseEntity<String> getStatus(@PathVariable Long paymentId) {
        return ResponseEntity.ok(service.getPaymentStatus(paymentId));
    }

    @PutMapping("/{paymentId}/status")
    @Operation(summary = "Manually update payment status — admin/system use")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long paymentId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam Payment.PaymentStatus status) {
        requireAdmin(role);
        service.updateStatus(paymentId, status);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Receipt
    // -------------------------------------------------------------------------

    @GetMapping("/{paymentId}/receipt")
    @Operation(summary = "Download PDF receipt for a payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF receipt returned"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<Resource> receipt(@PathVariable Long paymentId,
                                            @RequestHeader("X-User-Id") Long userId,
                                            @RequestHeader(value = "X-User-Role", required = false) String role) {
        requireOwnerOrAdmin(service.getPayment(paymentId), userId, role);
        String receiptPath = service.generateReceipt(paymentId);
        Resource resource = new FileSystemResource(receiptPath);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=receipt_" + paymentId + ".pdf")
                .body(resource);
    }

    // -------------------------------------------------------------------------
    // Revenue
    // -------------------------------------------------------------------------

    @GetMapping("/revenue")
    @Operation(summary = "Platform-wide total revenue for a date range")
    public ResponseEntity<BigDecimal> revenue(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        requireAdmin(role);
        return ResponseEntity.ok(service.getTotalRevenue(start, end));
    }

    @GetMapping("/revenue/lot/{lotId}")
    @Operation(summary = "Revenue for a specific parking lot in a date range")
    public ResponseEntity<BigDecimal> revenueByLot(
            @PathVariable Long lotId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        requireAdmin(role);
        return ResponseEntity.ok(service.getRevenueByLot(lotId, start, end));
    }

    // -------------------------------------------------------------------------
    // Transaction history
    // -------------------------------------------------------------------------

    @GetMapping("/history")
    @Operation(summary = "Get all transactions within a date range")
    public ResponseEntity<List<PaymentDTO>> history(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        requireAdmin(role);
        return ResponseEntity.ok(service.getTransactionHistory(start, end));
    }

    // -------------------------------------------------------------------------
    // Health check
    // -------------------------------------------------------------------------

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Service is running");
    }

    private void requireOwnerOrAdmin(PaymentDTO payment, Long userId, String role) {
        if (isAdmin(role)) {
            return;
        }
        if (userId == null || payment.getUserId() == null || !payment.getUserId().equals(userId)) {
            throw new SecurityException("You are not allowed to access this payment");
        }
    }

    private void requireAdmin(String role) {
        if (!isAdmin(role)) {
            throw new SecurityException("Admin or system role is required");
        }
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role) || "SYSTEM".equalsIgnoreCase(role);
    }
}
