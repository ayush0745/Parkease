package com.parkease.notification.service;

import com.parkease.notification.dto.NotificationDTO;
import com.parkease.notification.entity.Notification;
import com.parkease.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final EmailService emailService;
    private final SmsService smsService;

    public NotificationDTO send(NotificationDTO dto) {
        Notification notification = Notification.builder()
                .recipientId(dto.getRecipientId())
                .type(dto.getType())
                .title(dto.getTitle())
                .message(dto.getMessage())
                .channel(dto.getChannel())
                .relatedId(dto.getRelatedId())
                .relatedType(dto.getRelatedType())
                .recipientEmail(dto.getRecipientEmail())
                .recipientPhone(dto.getRecipientPhone())
                .isRead(false)
                .build();

        dispatch(notification);
        return toDto(repository.save(notification));
    }

    public List<NotificationDTO> sendBulk(List<NotificationDTO> notifications) {
        return notifications.stream().map(this::send).toList();
    }

    @Transactional(readOnly = true)
    public NotificationDTO getById(Long notificationId) {
        return repository.findById(notificationId)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getByRecipient(Long recipientId, Pageable pageable) {
        return repository.findByRecipientId(recipientId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUnread(Long recipientId, Pageable pageable) {
        return repository.findByRecipientIdAndIsReadFalse(recipientId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientId) {
        return repository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setIsRead(true);
        repository.save(notification);
    }

    public void markAllRead(Long recipientId) {
        List<Notification> notifications = repository.findByRecipientId(recipientId);
        notifications.forEach(notification -> notification.setIsRead(true));
        repository.saveAll(notifications);
    }

    public void deleteNotification(Long notificationId) {
        repository.deleteById(notificationId);
    }

    public void sendEmail(String toEmail, String subject, String body) {
        emailService.sendEmail(toEmail, subject, body);
    }

    public void sendSMS(String phoneNumber, String message) {
        smsService.sendSms(phoneNumber, message);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getByType(Notification.NotificationType type) {
        return repository.findByType(type).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getByRelatedId(Long relatedId) {
        return repository.findByRelatedId(relatedId).stream().map(this::toDto).toList();
    }

    private void dispatch(Notification notification) {
        try {
            if (notification.getChannel() == Notification.NotificationChannel.EMAIL && notification.getRecipientEmail() != null) {
                emailService.sendEmail(notification.getRecipientEmail(), notification.getTitle(), notification.getMessage());
            } else if (notification.getChannel() == Notification.NotificationChannel.SMS && notification.getRecipientPhone() != null) {
                smsService.sendSms(notification.getRecipientPhone(), notification.getMessage());
            }
        } catch (Exception ex) {
            log.warn("Notification dispatch failed for recipient {}: {}", notification.getRecipientId(), ex.getMessage());
        }
    }

    private NotificationDTO toDto(Notification entity) {
        return NotificationDTO.builder()
                .notificationId(entity.getNotificationId())
                .recipientId(entity.getRecipientId())
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .channel(entity.getChannel())
                .relatedId(entity.getRelatedId())
                .relatedType(entity.getRelatedType())
                .isRead(entity.getIsRead())
                .sentAt(entity.getSentAt())
                .recipientEmail(entity.getRecipientEmail())
                .recipientPhone(entity.getRecipientPhone())
                .build();
    }
}
