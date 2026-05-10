package com.workshop.outbox.after.fulfillment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    public static final String QUEUE = "fulfillment-service.order.placed";
    public static final String DLQ = "fulfillment-service.order.placed.dlq";
    public static final String DLX = "fulfillment-service.dlx";
    public static final String EXCHANGE = "orders.exchange";
    public static final String ROUTING_KEY = "order.placed";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange ordersExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange fulfillmentDeadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue fulfillmentQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue fulfillmentDlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding fulfillmentQueueBinding(Queue fulfillmentQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(fulfillmentQueue).to(ordersExchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding fulfillmentDlqBinding(Queue fulfillmentDlq, DirectExchange fulfillmentDeadLetterExchange) {
        return BindingBuilder.bind(fulfillmentDlq).to(fulfillmentDeadLetterExchange).with(DLQ);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
