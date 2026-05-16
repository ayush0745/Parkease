package com.parkease.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for ParkEase payment events.
 *
 * Exchange:  parkease.events  (topic)
 * Queues:
 *   - payment.paid.queue     → routing key: payment.paid
 *   - payment.refunded.queue → routing key: payment.refunded
 *   - payment.failed.queue   → routing key: payment.failed
 *
 * FIX: original config declared exchange only — missing Queue and Binding beans
 * which caused RabbitMQ to fail silently on startup.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "parkease.events";

    // Queue names
    public static final String QUEUE_PAYMENT_PAID     = "payment.paid.queue";
    public static final String QUEUE_PAYMENT_REFUNDED = "payment.refunded.queue";
    public static final String QUEUE_PAYMENT_FAILED   = "payment.failed.queue";

    // Routing keys
    public static final String KEY_PAYMENT_PAID     = "payment.paid";
    public static final String KEY_PAYMENT_REFUNDED = "payment.refunded";
    public static final String KEY_PAYMENT_FAILED   = "payment.failed";
    public static final String KEY_STATUS_UPDATED   = "payment.status.updated";

    @Bean
    public TopicExchange parkeaseExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // ---- Queues ----

    @Bean
    public Queue paymentPaidQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT_PAID).build();
    }

    @Bean
    public Queue paymentRefundedQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT_REFUNDED).build();
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT_FAILED).build();
    }

    // ---- Bindings ----

    @Bean
    public Binding bindingPaid(Queue paymentPaidQueue, TopicExchange parkeaseExchange) {
        return BindingBuilder.bind(paymentPaidQueue).to(parkeaseExchange).with(KEY_PAYMENT_PAID);
    }

    @Bean
    public Binding bindingRefunded(Queue paymentRefundedQueue, TopicExchange parkeaseExchange) {
        return BindingBuilder.bind(paymentRefundedQueue).to(parkeaseExchange).with(KEY_PAYMENT_REFUNDED);
    }

    @Bean
    public Binding bindingFailed(Queue paymentFailedQueue, TopicExchange parkeaseExchange) {
        return BindingBuilder.bind(paymentFailedQueue).to(parkeaseExchange).with(KEY_PAYMENT_FAILED);
    }

    // ---- Infrastructure ----

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
