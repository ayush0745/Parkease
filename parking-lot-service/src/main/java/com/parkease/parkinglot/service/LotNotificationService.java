package com.parkease.parkinglot.service;

import com.parkease.parkinglot.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * LotNotificationService — publishes lot lifecycle events to RabbitMQ.
 * The notification-service consumes these and dispatches in-app alerts to admins/managers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotNotificationService {

    private final RabbitTemplate rabbitTemplate;

    /** Published when a manager submits a new lot — notification-service notifies all admins. */
    public void notifyAdminsLotPending(Long lotId, String lotName, Long managerId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "lot.pending");
            event.put("lotId", lotId);
            event.put("lotName", lotName);
            event.put("managerId", managerId);
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.LOT_PENDING, event);
            log.info("Published lot.pending event for lotId={} managerId={}", lotId, managerId);
        } catch (Exception e) {
            log.warn("Failed to publish lot.pending event for lotId={}: {}", lotId, e.getMessage());
        }
    }

    /** Published when admin approves — notification-service notifies the manager. */
    public void notifyManagerLotApproved(Long lotId, String lotName, Long managerId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "lot.approved");
            event.put("lotId", lotId);
            event.put("lotName", lotName);
            event.put("managerId", managerId);
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.LOT_PENDING, event);
            log.info("Published lot.approved event for lotId={} managerId={}", lotId, managerId);
        } catch (Exception e) {
            log.warn("Failed to publish lot.approved event for lotId={}: {}", lotId, e.getMessage());
        }
    }

    /** Published when admin rejects — notification-service notifies the manager. */
    public void notifyManagerLotRejected(Long lotId, String lotName, Long managerId, String reason) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "lot.rejected");
            event.put("lotId", lotId);
            event.put("lotName", lotName);
            event.put("managerId", managerId);
            event.put("reason", reason);
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.LOT_PENDING, event);
            log.info("Published lot.rejected event for lotId={} managerId={}", lotId, managerId);
        } catch (Exception e) {
            log.warn("Failed to publish lot.rejected event for lotId={}: {}", lotId, e.getMessage());
        }
    }
}
