package com.parkease.notification.controller;

import com.parkease.notification.dto.NotificationDTO;
import com.parkease.notification.entity.Notification;
import com.parkease.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    public ResponseEntity<NotificationDTO> send(@Valid @RequestBody NotificationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.send(dto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<NotificationDTO>> sendBulk(@RequestBody List<@Valid NotificationDTO> notifications) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.sendBulk(notifications));
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationDTO>> all() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationDTO> get(@PathVariable Long notificationId) {
        return ResponseEntity.ok(service.getById(notificationId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<NotificationDTO>> byType(@PathVariable Notification.NotificationType type) {
        return ResponseEntity.ok(service.getByType(type));
    }

    @GetMapping("/related/{relatedId}")
    public ResponseEntity<List<NotificationDTO>> byRelatedId(@PathVariable Long relatedId) {
        return ResponseEntity.ok(service.getByRelatedId(relatedId));
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<Page<NotificationDTO>> byRecipient(@PathVariable Long recipientId, Pageable pageable) {
        return ResponseEntity.ok(service.getByRecipient(recipientId, pageable));
    }

    @GetMapping("/recipient/{recipientId}/unread")
    public ResponseEntity<Page<NotificationDTO>> unread(@PathVariable Long recipientId, Pageable pageable) {
        return ResponseEntity.ok(service.getUnread(recipientId, pageable));
    }

    @GetMapping("/recipient/{recipientId}/unread/count")
    public ResponseEntity<Long> unreadCount(@PathVariable Long recipientId) {
        return ResponseEntity.ok(service.getUnreadCount(recipientId));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long notificationId) {
        service.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/recipient/{recipientId}/read")
    public ResponseEntity<Void> markAllRead(@PathVariable Long recipientId) {
        service.markAllRead(recipientId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> delete(@PathVariable Long notificationId) {
        service.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
