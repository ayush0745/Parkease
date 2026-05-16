package com.parkease.notification.repository;

import com.parkease.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientId(Long recipientId);
    Page<Notification> findByRecipientId(Long recipientId, Pageable pageable);
    List<Notification> findByRecipientIdAndIsRead(Long recipientId, Boolean isRead);
    Page<Notification> findByRecipientIdAndIsReadFalse(Long recipientId, Pageable pageable);
    long countByRecipientIdAndIsRead(Long recipientId, Boolean isRead);
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    List<Notification> findByType(Notification.NotificationType type);
    List<Notification> findByRelatedId(Long relatedId);
    List<Notification> findByRelatedIdAndRelatedType(Long relatedId, String relatedType);
    void deleteByNotificationId(Long notificationId);
}
