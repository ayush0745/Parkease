package com.parkease.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE            = "parkease.events";
    public static final String NOTIFICATION_QUEUE  = "notification.events";
    public static final String LOT_PENDING_QUEUE   = "lot.pending";
    public static final String PASSWORD_RESET_QUEUE = "user.password_reset";

    @Bean
    TopicExchange parkeaseExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean Queue notificationQueue()     { return QueueBuilder.durable(NOTIFICATION_QUEUE).build(); }
    @Bean Queue bookingCreatedQueue()   { return QueueBuilder.durable("booking.created").build(); }
    @Bean Queue bookingCancelledQueue() { return QueueBuilder.durable("booking.cancelled").build(); }
    @Bean Queue bookingCheckoutQueue()  { return QueueBuilder.durable("booking.checkout").build(); }
    @Bean Queue lotPendingQueue()       { return QueueBuilder.durable(LOT_PENDING_QUEUE).build(); }
    @Bean Queue passwordResetQueue()    { return QueueBuilder.durable(PASSWORD_RESET_QUEUE).build(); }

    @Bean Binding notificationBinding(TopicExchange parkeaseExchange)    { return BindingBuilder.bind(notificationQueue()).to(parkeaseExchange).with("booking.*"); }
    @Bean Binding bookingCreatedBinding(TopicExchange parkeaseExchange)  { return BindingBuilder.bind(bookingCreatedQueue()).to(parkeaseExchange).with("booking.created"); }
    @Bean Binding bookingCancelledBinding(TopicExchange parkeaseExchange){ return BindingBuilder.bind(bookingCancelledQueue()).to(parkeaseExchange).with("booking.cancelled"); }
    @Bean Binding bookingCheckoutBinding(TopicExchange parkeaseExchange) { return BindingBuilder.bind(bookingCheckoutQueue()).to(parkeaseExchange).with("booking.checkout"); }
    @Bean Binding lotPendingBinding(TopicExchange parkeaseExchange)      { return BindingBuilder.bind(lotPendingQueue()).to(parkeaseExchange).with("lot.pending"); }
    @Bean Binding passwordResetBinding(TopicExchange parkeaseExchange)   { return BindingBuilder.bind(passwordResetQueue()).to(parkeaseExchange).with("user.password_reset"); }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setMissingQueuesFatal(false);
        return factory;
    }
}
