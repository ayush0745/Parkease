package com.parkease.notification.service;

import com.parkease.notification.dto.NotificationDTO;
import com.parkease.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    NotificationDTO send(NotificationDTO dto);
    List<NotificationDTO> sendBulk(List<NotificationDTO> notifications);
    NotificationDTO getById(Long notificationId);
    Page<NotificationDTO> getByRecipient(Long recipientId, Pageable pageable);
    Page<NotificationDTO> getUnread(Long recipientId, Pageable pageable);
    long getUnreadCount(Long recipientId);
    void markAsRead(Long notificationId);
    void markAllRead(Long recipientId);
    void deleteNotification(Long notificationId);
    void sendEmail(String toEmail, String subject, String body);
    void sendSMS(String phoneNumber, String message);
    List<NotificationDTO> getAll();
    List<NotificationDTO> getByType(Notification.NotificationType type);
    List<NotificationDTO> getByRelatedId(Long relatedId);
}
