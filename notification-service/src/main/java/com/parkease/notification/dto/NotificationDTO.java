package com.parkease.notification.dto;

import com.parkease.notification.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long notificationId;

    @NotNull
    private Long recipientId;

    @NotNull
    private Notification.NotificationType type;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private Notification.NotificationChannel channel;

    private Long relatedId;
    private String relatedType;
    private Boolean isRead;
    private LocalDateTime sentAt;
    private String recipientEmail;
    private String recipientPhone;
}
