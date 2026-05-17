package com.parkease.parkinglot.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE    = "parkease.events";
    public static final String LOT_PENDING = "lot.pending";

    @Bean
    TopicExchange parkeaseExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue lotPendingQueue() {
        return QueueBuilder.durable(LOT_PENDING).build();
    }

    @Bean
    Binding lotPendingBinding(TopicExchange parkeaseExchange) {
        return BindingBuilder.bind(lotPendingQueue()).to(parkeaseExchange).with(LOT_PENDING);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
