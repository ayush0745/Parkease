package com.parkease.booking.feign;

import com.parkease.booking.dto.ProcessPaymentRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentServiceFallback implements PaymentServiceClient {
    public Map<String, Object> processPayment(Long userId, ProcessPaymentRequest request) {
        return Map.of("status", "PENDING", "message", "payment-service unavailable");
    }
}
