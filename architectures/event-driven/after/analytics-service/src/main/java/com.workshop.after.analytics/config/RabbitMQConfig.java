package com.workshop.after.analytics.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "analytics-service.user.events";
    public static final String DLQ = "analytics-service.user.events.dlq";
    public static final String DLX = "analytics-service.dlx";
    public static final String EXCHANGE = "user.events";
    public static final String ROUTING_KEY = "user.profile.changed";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue analyticsDlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public TopicExchange userEventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding analyticsQueueBinding(Queue analyticsQueue,
                                         TopicExchange userEventsExchange) {
        return BindingBuilder.bind(analyticsQueue).to(userEventsExchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding(Queue analyticsDlq,
                              DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(analyticsDlq).to(deadLetterExchange).with(DLQ);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setPrefetchCount(5);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
