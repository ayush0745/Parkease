package com.parkease.notification.service;

import com.parkease.notification.config.RabbitConfig;
import com.parkease.notification.dto.NotificationDTO;
import com.parkease.notification.dto.NotificationEvent;
import com.parkease.notification.dto.PasswordResetEvent;
import com.parkease.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final SmsService smsService;

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void onEvent(Map<String, Object> event) {
        String eventType = String.valueOf(event.get("eventType"));
        Long userId = toLong(event.get("userId"));
        Long relatedId = toLong(event.getOrDefault("bookingId", event.get("paymentId")));

        Notification.NotificationType type = eventType.startsWith("payment")
                ? Notification.NotificationType.PAYMENT
                : notificationTypeForBooking(eventType);

        // 1. Send app notification to the driver
        notificationService.send(NotificationDTO.builder()
                .recipientId(userId)
                .type(type)
                .title(titleFor(eventType))
                .message("ParkEase update: " + eventType.replace('.', ' '))
                .channel(Notification.NotificationChannel.APP)
                .relatedId(relatedId)
                .relatedType(eventType.startsWith("payment") ? "PAYMENT" : "BOOKING")
                .build());

        // 2. Query parking-lot-service to see if we can notify the manager of this lot
        Long lotId = toLong(event.get("lotId"));
        if (lotId != null && lotId > 0) {
            try {
                org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                String url = "http://localhost:8082/api/v1/lots/" + lotId;
                @SuppressWarnings("unchecked")
                Map<String, Object> lot = restTemplate.getForObject(url, Map.class);
                if (lot != null && lot.containsKey("managerId")) {
                    Long managerId = toLong(lot.get("managerId"));
                    String lotName = String.valueOf(lot.getOrDefault("name", "Parking Lot #" + lotId));
                    if (managerId != null && managerId > 0) {
                        notificationService.send(NotificationDTO.builder()
                                .recipientId(managerId)
                                .type(Notification.NotificationType.BOOKING)
                                .title(managerTitleFor(eventType, lotName))
                                .message(managerMessageFor(eventType, lotName, relatedId))
                                .channel(Notification.NotificationChannel.APP)
                                .relatedId(relatedId)
                                .relatedType("BOOKING")
                                .build());
                    }
                }
            } catch (Exception ex) {
                log.warn("Failed to send manager notification for lotId={}: {}", lotId, ex.getMessage());
            }
        }
    }
    
    @RabbitListener(queues = "booking.created")
    public void handleBookingCreated(NotificationEvent event) {
        log.info("Received booking created event: {}", event.getBookingId());
        
        String subject = "Booking Confirmation - ParkEase";
        String message = String.format(
            "Dear %s,\n\nYour parking booking has been confirmed!\n\n" +
            "Booking ID: %s\nLot: %s\nSpot: %s\nStart Time: %s\nAmount: ₹%s\n\n" +
            "Thank you for choosing ParkEase!",
            event.getUserName(), event.getBookingId(), event.getLotName(), 
            event.getSpotNumber(), event.getStartTime(), event.getAmount()
        );
        
        sendNotification(event, subject, message);
    }
    
    @RabbitListener(queues = "booking.cancelled")
    public void handleBookingCancelled(NotificationEvent event) {
        log.info("Received booking cancelled event: {}", event.getBookingId());
        
        String subject = "Booking Cancelled - ParkEase";
        String message = String.format(
            "Dear %s,\n\nYour parking booking has been cancelled.\n\n" +
            "Booking ID: %s\nLot: %s\n\n" +
            "If you have any questions, please contact support.",
            event.getUserName(), event.getBookingId(), event.getLotName()
        );
        
        sendNotification(event, subject, message);
    }
    
    @RabbitListener(queues = "booking.checkout")
    public void handleBookingCheckout(NotificationEvent event) {
        log.info("Received booking checkout event: {}", event.getBookingId());
        
        String subject = "Checkout Summary - ParkEase";
        String message = String.format(
            "Dear %s,\n\nThank you for using ParkEase!\n\n" +
            "Booking ID: %s\nLot: %s\nSpot: %s\nTotal Amount: ₹%s\n\n" +
            "Your receipt has been generated and will be sent separately.",
            event.getUserName(), event.getBookingId(), event.getLotName(), 
            event.getSpotNumber(), event.getAmount()
        );
        
        sendNotification(event, subject, message);
    }

    private Notification.NotificationType notificationTypeForBooking(String eventType) {
        if (eventType.contains("checkin")) return Notification.NotificationType.CHECKIN;
        if (eventType.contains("checkout")) return Notification.NotificationType.CHECKOUT;
        if (eventType.contains("expired")) return Notification.NotificationType.EXPIRY;
        return Notification.NotificationType.BOOKING;
    }

    private String titleFor(String eventType) {
        return switch (eventType) {
            case "booking.created" -> "Booking confirmed";
            case "booking.checkin" -> "Check-in confirmed";
            case "booking.checkout" -> "Checkout completed";
            case "booking.cancelled" -> "Booking cancelled";
            case "payment.paid" -> "Payment received";
            case "payment.refunded" -> "Payment refunded";
            default -> "ParkEase notification";
        };
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) return number.longValue();
        if (value == null) return 0L;
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    private String managerTitleFor(String eventType, String lotName) {
        return switch (eventType) {
            case "booking.created" -> "New Booking Confirmed - " + lotName;
            case "booking.checkin" -> "Driver Checked In - " + lotName;
            case "booking.checkout" -> "Driver Checked Out - " + lotName;
            case "booking.cancelled" -> "Booking Cancelled - " + lotName;
            case "payment.paid" -> "Payment Received - " + lotName;
            case "payment.refunded" -> "Payment Refunded - " + lotName;
            default -> "Lot Activity Update - " + lotName;
        };
    }

    private String managerMessageFor(String eventType, String lotName, Long bookingId) {
        return switch (eventType) {
            case "booking.created" -> String.format("A new booking (ID: %d) was successfully reserved at your lot '%s'.", bookingId, lotName);
            case "booking.checkin" -> String.format("A driver has successfully checked-in for booking (ID: %d) at your lot '%s'.", bookingId, lotName);
            case "booking.checkout" -> String.format("A driver has successfully checked-out for booking (ID: %d) at your lot '%s'.", bookingId, lotName);
            case "booking.cancelled" -> String.format("Booking (ID: %d) has been cancelled at your lot '%s'. The spot is now available.", bookingId, lotName);
            case "payment.paid" -> String.format("Payment was received successfully for booking (ID: %d) at your lot '%s'.", bookingId, lotName);
            case "payment.refunded" -> String.format("A refund has been issued for booking (ID: %d) at your lot '%s'.", bookingId, lotName);
            default -> String.format("Driver performed action '%s' on booking (ID: %d) at your lot '%s'.", eventType.replace('.', ' '), bookingId, lotName);
        };
    }
    
    /**
     * Handles lot lifecycle events published by parking-lot-service:
     *   - lot.pending  → notify all ADMINs that a new lot needs review
     *   - lot.approved → notify the MANAGER that their lot was approved
     *   - lot.rejected → notify the MANAGER that their lot was rejected
     */
    @RabbitListener(queues = RabbitConfig.LOT_PENDING_QUEUE)
    public void handleLotEvent(Map<String, Object> event) {
        String eventType = String.valueOf(event.get("eventType"));
        Long lotId      = toLong(event.get("lotId"));
        Long managerId  = toLong(event.get("managerId"));
        String lotName  = String.valueOf(event.getOrDefault("lotName", "Parking Lot #" + lotId));
        String reason   = String.valueOf(event.getOrDefault("reason", "No reason provided"));

        log.info("Received lot event: type={} lotId={} managerId={}", eventType, lotId, managerId);

        switch (eventType) {
            case "lot.pending" -> {
                // Notify all ADMIN users via auth-service internal endpoint
                try {
                    org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                    String url = "http://localhost:8081/api/v1/auth/internal/users-by-role?role=ADMIN";
                    @SuppressWarnings("unchecked")
                    java.util.List<java.util.Map<String, Object>> admins = restTemplate.getForObject(url, java.util.List.class);
                    if (admins != null) {
                        for (java.util.Map<String, Object> admin : admins) {
                            Long adminId = toLong(admin.get("userId"));
                            if (adminId != null && adminId > 0) {
                                notificationService.send(NotificationDTO.builder()
                                        .recipientId(adminId)
                                        .type(Notification.NotificationType.BOOKING)
                                        .title("🏢 New Lot Pending Approval")
                                        .message(String.format(
                                                "Manager #%d submitted parking lot '%s' (ID: %d) for approval. Tap to review.",
                                                managerId, lotName, lotId))
                                        .channel(Notification.NotificationChannel.APP)
                                        .relatedId(lotId)
                                        .relatedType("LOT")
                                        .build());
                                log.info("Sent lot.pending notification to admin #{}", adminId);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to notify admins for lot.pending (lotId={}): {}", lotId, e.getMessage());
                }
            }
            case "lot.approved" -> {
                notificationService.send(NotificationDTO.builder()
                        .recipientId(managerId)
                        .type(Notification.NotificationType.BOOKING)
                        .title("✅ Your Lot Has Been Approved!")
                        .message(String.format(
                                "Congratulations! Your parking lot '%s' (ID: %d) has been approved by the admin and is now live on ParkEase.",
                                lotName, lotId))
                        .channel(Notification.NotificationChannel.APP)
                        .relatedId(lotId)
                        .relatedType("LOT")
                        .build());
                log.info("Sent lot.approved notification to manager #{}", managerId);
            }
            case "lot.rejected" -> {
                notificationService.send(NotificationDTO.builder()
                        .recipientId(managerId)
                        .type(Notification.NotificationType.BOOKING)
                        .title("❌ Lot Approval Rejected")
                        .message(String.format(
                                "Your parking lot '%s' (ID: %d) was not approved. Reason: %s. Please update details and resubmit.",
                                lotName, lotId, reason))
                        .channel(Notification.NotificationChannel.APP)
                        .relatedId(lotId)
                        .relatedType("LOT")
                        .build());
                log.info("Sent lot.rejected notification to manager #{}", managerId);
            }
            default -> log.warn("Unknown lot event type: {}", eventType);
        }
    }

    private void sendNotification(NotificationEvent event, String subject, String message) {
        try {
            notificationService.send(NotificationDTO.builder()
                    .recipientId(event.getUserId())
                    .type(Notification.NotificationType.BOOKING)
                    .title(subject)
                    .message(message)
                    .channel(Notification.NotificationChannel.APP)
                    .relatedId(event.getBookingId())
                    .relatedType("BOOKING")
                    .build());

            if (event.getUserEmail() != null) {
                emailService.sendEmail(event.getUserEmail(), subject, message);
            }

            if (event.getPhone() != null &&
                ("BOOKING_CREATED".equals(event.getEventType()) || "BOOKING_EXPIRED".equals(event.getEventType()))) {
                smsService.sendSms(event.getPhone(), message);
            }
        } catch (Exception e) {
            log.error("Failed to send notification for event: {}", event.getEventType(), e);
        }
    }

    @RabbitListener(queues = RabbitConfig.PASSWORD_RESET_QUEUE)
    public void handlePasswordReset(PasswordResetEvent event) {
        log.info("Received password reset event for email: {}", event.getEmail());
        try {
            String resetLink = "http://localhost:4200/reset-password?token=" + event.getToken();
            String subject = "ParkEase - Password Reset Request";
            String body = "Hello,\n\n" +
                    "We received a request to reset your password. Please copy the token below or use the link to reset your password:\n\n" +
                    "Token: " + event.getToken() + "\n\n" +
                    "Link: " + resetLink + "\n\n" +
                    "If you didn't request this, you can safely ignore this email.\n\n" +
                    "Thanks,\nThe ParkEase Team";

            emailService.sendEmail(event.getEmail(), subject, body);
            log.info("Password reset email sent to {}", event.getEmail());
        } catch (Exception e) {
            log.error("Error processing password reset event for email {}: {}", event.getEmail(), e.getMessage(), e);
        }
    }
}
