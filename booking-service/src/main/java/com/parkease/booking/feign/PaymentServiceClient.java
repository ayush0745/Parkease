package com.parkease.booking.feign;

import com.parkease.booking.dto.ProcessPaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "payment-service", fallback = PaymentServiceFallback.class)
public interface PaymentServiceClient {
    @PostMapping("/api/v1/payments/process")
    Map<String, Object> processPayment(@RequestHeader("X-User-Id") Long userId, @RequestBody ProcessPaymentRequest request);
}
