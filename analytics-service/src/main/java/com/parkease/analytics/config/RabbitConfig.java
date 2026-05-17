package com.parkease.analytics.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "parkease.events";

    @Bean
    TopicExchange parkeaseExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean Queue bookingCreatedQueue()   { return QueueBuilder.durable("booking.created").build(); }
    @Bean Queue bookingCheckinQueue()   { return QueueBuilder.durable("booking.checkin").build(); }
    @Bean Queue bookingCheckoutQueue()  { return QueueBuilder.durable("booking.checkout").build(); }
    @Bean Queue paymentCompletedQueue() { return QueueBuilder.durable("payment.completed").build(); }

    @Bean Binding bookingCreatedBinding(TopicExchange parkeaseExchange)   { return BindingBuilder.bind(bookingCreatedQueue()).to(parkeaseExchange).with("booking.created"); }
    @Bean Binding bookingCheckinBinding(TopicExchange parkeaseExchange)   { return BindingBuilder.bind(bookingCheckinQueue()).to(parkeaseExchange).with("booking.checkin"); }
    @Bean Binding bookingCheckoutBinding(TopicExchange parkeaseExchange)  { return BindingBuilder.bind(bookingCheckoutQueue()).to(parkeaseExchange).with("booking.checkout"); }
    @Bean Binding paymentCompletedBinding(TopicExchange parkeaseExchange) { return BindingBuilder.bind(paymentCompletedQueue()).to(parkeaseExchange).with("payment.completed"); }

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
