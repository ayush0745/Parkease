package com.parkease.booking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "parkease.events";
    
    // Queue names
    public static final String BOOKING_CREATED_QUEUE = "booking.created";
    public static final String BOOKING_CANCELLED_QUEUE = "booking.cancelled";
    public static final String BOOKING_CHECKIN_QUEUE = "booking.checkin";
    public static final String BOOKING_CHECKOUT_QUEUE = "booking.checkout";
    public static final String BOOKING_EXPIRED_QUEUE = "booking.expired";
    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed";

    @Bean
    TopicExchange parkeaseExchange() { return new TopicExchange(EXCHANGE, true, false); }
    
    @Bean
    public Queue bookingCreatedQueue() {
        return QueueBuilder.durable(BOOKING_CREATED_QUEUE).build();
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return QueueBuilder.durable(BOOKING_CANCELLED_QUEUE).build();
    }

    @Bean
    public Queue bookingCheckinQueue() {
        return QueueBuilder.durable(BOOKING_CHECKIN_QUEUE).build();
    }

    @Bean
    public Queue bookingCheckoutQueue() {
        return QueueBuilder.durable(BOOKING_CHECKOUT_QUEUE).build();
    }

    @Bean
    public Queue bookingExpiredQueue() {
        return QueueBuilder.durable(BOOKING_EXPIRED_QUEUE).build();
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE).build();
    }

    @Bean
    MessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
