package com.parkease.booking.service;

import com.parkease.booking.dto.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishBookingCreated(BookingEvent event) {
        event.setEventType("BOOKING_CREATED");
        publishEvent("booking.created", event);
    }
    
    public void publishBookingCancelled(BookingEvent event) {
        event.setEventType("BOOKING_CANCELLED");
        publishEvent("booking.cancelled", event);
    }
    
    public void publishCheckIn(BookingEvent event) {
        event.setEventType("BOOKING_CHECKIN");
        publishEvent("booking.checkin", event);
    }
    
    public void publishCheckOut(BookingEvent event) {
        event.setEventType("BOOKING_CHECKOUT");
        publishEvent("booking.checkout", event);
    }
    
    public void publishBookingExpired(BookingEvent event) {
        event.setEventType("BOOKING_EXPIRED");
        publishEvent("booking.expired", event);
    }
    
    private void publishEvent(String routingKey, BookingEvent event) {
        try {
            rabbitTemplate.convertAndSend("parkease.exchange", routingKey, event);
            log.info("Published event: {} for booking: {}", event.getEventType(), event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to publish event: {} for booking: {}", event.getEventType(), event.getBookingId(), e);
        }
    }
}