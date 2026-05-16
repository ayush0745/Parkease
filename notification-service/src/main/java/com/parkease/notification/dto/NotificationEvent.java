package com.parkease.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String eventType;
    private Long userId;
    private String userEmail;
    private String userName;
    private String phone;
    private String subject;
    private String message;
    private Map<String, Object> data;
    private LocalDateTime timestamp;
    
    // Booking specific fields
    private Long bookingId;
    private String lotName;
    private String spotNumber;
    private BigDecimal amount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}