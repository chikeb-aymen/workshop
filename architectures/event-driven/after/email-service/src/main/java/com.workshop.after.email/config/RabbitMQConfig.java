package com.workshop.after.email.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience (DLQ):
 *   Every queue has a paired Dead Letter Queue.
 *   Dead Letter Queue (DLQ) is a dedicated Kafka topic that stores messages that consumers failed to process,
 *   preventing blocking of the main pipeline
 *
 * Avoid callback storms:
 *   prefetchCount = 1 means this consumer processes one event at a time,
 *   avoiding overwhelming the user-service with burst fetch requests.
 *   Increase when throughput is measured and safe.
 *
 * Avoid tight coupling:
 *   EmailService owns its own queue declaration. UserService doesn't know
 *   this queue exists. Adding a new consumer = new queue, zero changes
 *   to UserService or this config.
 */
@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "email-service.user.events";
    public static final String DLQ = "email-service.user.events.dlq";
    public static final String DLX = "email-service.dlx";
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
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding emailQueueBinding(Queue emailQueue, TopicExchange userEventsExchange) {
        return BindingBuilder
                .bind(emailQueue)
                .to(userEventsExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding(Queue emailDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder
                .bind(emailDlq)
                .to(deadLetterExchange)
                .with(DLQ);
    }

    /**
     * TopicExchange bean must match what UserService declared.
     * Spring deduplicates if both services connect to the same broker.
     */
    @Bean
    public TopicExchange userEventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {

        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setPrefetchCount(1);                 // avoid callback storms
        factory.setDefaultRequeueRejected(false);    // send to DLQ, don't requeue forever
        return factory;
    }
}
